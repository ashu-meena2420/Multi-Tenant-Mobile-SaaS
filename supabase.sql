-- ============================================================================
-- PropManager PostgreSQL Database Schema for Supabase
-- Place this inside your Supabase SQL Editor and execute it to create tables.
-- ============================================================================

-- 1. Custom Types / Enums
CREATE TYPE user_role AS ENUM ('LANDLORD', 'RESIDENT', 'ADMIN');
CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'OVERDUE');
CREATE TYPE request_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE request_status AS ENUM ('PENDING', 'IN_PROGRESS', 'RESOLVED');

-- 2. Tables

-- Users Table
CREATE TABLE public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    role user_role NOT NULL,
    phone TEXT DEFAULT '',
    subscription_tier TEXT DEFAULT 'Free',
    landlord_id UUID REFERENCES auth.users(id) ON DELETE SET NULL
);

-- Properties Table
CREATE TABLE public.properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    rent_amount DOUBLE PRECISION NOT NULL,
    description TEXT DEFAULT '',
    image_url TEXT DEFAULT '',
    total_units INTEGER DEFAULT 1,
    occupied_units INTEGER DEFAULT 0
);

-- Residents Table
CREATE TABLE public.residents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    unit_number TEXT DEFAULT '',
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT DEFAULT '',
    lease_start TEXT DEFAULT '',
    lease_end TEXT DEFAULT '',
    is_active BOOLEAN DEFAULT TRUE
);

-- Payments / Invoices Table
CREATE TABLE public.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    resident_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    invoice_number TEXT NOT NULL,
    due_date TEXT NOT NULL,
    paid_date TEXT,
    status payment_status DEFAULT 'PENDING',
    notes TEXT DEFAULT ''
);

-- Maintenance Requests Table
CREATE TABLE public.maintenance_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    resident_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    property_id UUID NOT NULL REFERENCES public.properties(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    priority request_priority DEFAULT 'MEDIUM',
    status request_status DEFAULT 'PENDING',
    date_submitted TEXT NOT NULL,
    image_url TEXT DEFAULT ''
);

-- Documents Table
CREATE TABLE public.documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    landlord_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    resident_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    file_type TEXT NOT NULL,
    file_url TEXT DEFAULT '',
    date_uploaded TEXT NOT NULL
);

-- ============================================================================
-- Row Level Security (RLS) Configuration
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.properties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.residents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.maintenance_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.documents ENABLE ROW LEVEL SECURITY;

-- 1. Users Policies
CREATE POLICY "Allow authenticated read users" ON public.users
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "Allow users to update own profile" ON public.users
    FOR UPDATE TO authenticated USING (auth.uid() = id);

CREATE POLICY "Allow users to insert own profile" ON public.users
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);

-- 2. Properties Policies
CREATE POLICY "Allow landlords to CRUD properties" ON public.properties
    FOR ALL TO authenticated USING (auth.uid() = landlord_id);

CREATE POLICY "Allow residents to read properties" ON public.properties
    FOR SELECT TO authenticated USING (
        EXISTS (
            SELECT 1 FROM public.users 
            WHERE id = auth.uid() AND (role = 'RESIDENT' OR role = 'ADMIN')
        )
    );

-- 3. Residents Policies
CREATE POLICY "Allow landlords to CRUD residents" ON public.residents
    FOR ALL TO authenticated USING (auth.uid() = landlord_id);

CREATE POLICY "Allow residents to read own resident record" ON public.residents
    FOR SELECT TO authenticated USING (auth.uid() = id);

-- 4. Payments Policies
CREATE POLICY "Allow landlords to CRUD payments" ON public.payments
    FOR ALL TO authenticated USING (auth.uid() = landlord_id);

CREATE POLICY "Allow residents to read/update own payments" ON public.payments
    FOR SELECT TO authenticated USING (auth.uid() = resident_id);

CREATE POLICY "Allow residents to pay invoice (update status)" ON public.payments
    FOR UPDATE TO authenticated USING (auth.uid() = resident_id);

-- 5. Maintenance Requests Policies
CREATE POLICY "Allow landlords to read/update maintenance" ON public.maintenance_requests
    FOR ALL TO authenticated USING (auth.uid() = landlord_id);

CREATE POLICY "Allow residents to read/insert/update own requests" ON public.maintenance_requests
    FOR ALL TO authenticated USING (auth.uid() = resident_id);

-- 6. Documents Policies
CREATE POLICY "Allow landlords to CRUD documents" ON public.documents
    FOR ALL TO authenticated USING (auth.uid() = landlord_id);

CREATE POLICY "Allow residents to read shared documents" ON public.documents
    FOR SELECT TO authenticated USING (auth.uid() = resident_id);

-- ============================================================================
-- Profile Creation Trigger (GoTrue Auth to Public Users)
-- ============================================================================

-- Function to handle copying GoTrue sign-up data to our public.users table
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    default_role public.user_role := 'LANDLORD';
    chosen_role text;
    final_role public.user_role;
    landlord_uuid uuid := NULL;
BEGIN
    -- Extract chosen role from metadata
    IF new.raw_user_meta_data IS NOT NULL AND new.raw_user_meta_data ? 'role' THEN
        chosen_role := new.raw_user_meta_data->>'role';
        IF chosen_role = 'LANDLORD' THEN
            final_role := 'LANDLORD';
        ELSIF chosen_role = 'RESIDENT' THEN
            final_role := 'RESIDENT';
        ELSIF chosen_role = 'ADMIN' THEN
            final_role := 'ADMIN';
        ELSE
            final_role := default_role;
        END IF;
    ELSE
        final_role := default_role;
    END IF;

    -- If landlord, landlord_id is their own id
    IF final_role = 'LANDLORD' THEN
        landlord_uuid := new.id;
    END IF;

    INSERT INTO public.users (id, name, email, role, phone, landlord_id)
    VALUES (
        new.id,
        COALESCE(new.raw_user_meta_data->>'name', ''),
        new.email,
        final_role,
        COALESCE(new.raw_user_meta_data->>'phone', ''),
        landlord_uuid
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger execution rule after insertion in auth.users
CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
