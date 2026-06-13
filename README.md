# Multi-Tenant Property Management App (PropManager)

## This is an Android app I built during my internship at CODTECH

## Intern Project Details
- **INTERN ID:** CITS2358
- **FULL NAME:** Ashu meena
- **NO. OF WEEKS:** 6 Weeks
- **PROJECT NAME:** Multi-Tenant Mobile SaaS (Property Management Portal)
- **PROJECT SCOPE:** An Android property management application that allows landlords to manage their properties, assign residents, and handle rent payments and maintenance requests. The project uses a multi-tenant model with database-level security policies (Supabase RLS) to prevent landlords and residents from seeing each other's data.

## Project Description
PropManager is an Android app built to simplify property management for landlords and tenants. It supports three distinct user roles with different dashboards:

1. **Admin Panel:** A platform control center that lets administrators view all registered landlords and change their subscription plans (Free, Pro, Enterprise).
2. **Landlord Dashboard:** A screen where landlords can add or delete properties, link tenants to units, generate invoices for rent, and track maintenance tickets.
3. **Resident Portal:** A dashboard for tenants to check their lease start and end dates, view unpaid bills, simulate online credit card payments, submit maintenance requests with priority levels, and view shared lease documents.

## Technology Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (using a dark theme design with custom cards, badges, and a custom donut chart for rent statistics)
- **Architecture:** MVVM with Clean Architecture
- **Dependency Injection:** Hilt
- **Navigation:** AndroidX Navigation3 (handles backstack and route arguments cleanly)
- **Backend Service:** Supabase (Database, Auth, and Realtime listeners)
- **Networking Library:** Ktor Client (for connecting to Supabase APIs)

## Database Schema & Tenant Isolation
To make sure data is securely separated between different landlords and tenants, we set up Row-Level Security (RLS) policies in the Supabase PostgreSQL database. This ensures users can only read and write data they are authorized to access.

### Database Tables
- **users:** Stores user accounts, emails, phone numbers, and roles ('LANDLORD', 'RESIDENT', 'ADMIN').
- **properties:** Stores rental buildings/flats managed by landlords.
- **residents:** Stores lease agreements, units, and tenant profile mappings.
- **payments:** Stores rent bills, due dates, paid dates, and statuses ('PENDING', 'PAID', 'OVERDUE').
- **maintenance_requests:** Stores repairs submitted by residents, marked with a priority ('LOW', 'MEDIUM', 'HIGH') and status ('PENDING', 'IN_PROGRESS', 'RESOLVED').
- **documents:** Stores details of uploaded lease agreements or receipt files.

### Row-Level Security (RLS) Rules
The security policies are defined directly inside PostgreSQL:
- **Properties:** Landlords can only create, view, or update properties they created. Residents can only view the property details if they are linked to it.
- **Residents:** Landlords can manage tenant details. Residents can only read their own profile.
- **Payments:** Landlords can generate invoices. Residents can view their own invoices and update the status to PAID when making a simulated payment.
- **Maintenance Requests:** Tenants can write new requests. Landlords can view requests for their properties and update the ticket status.

### Auth Synchronization Trigger
We created a PostgreSQL trigger `on_auth_user_created` that automatically runs the function `public.handle_new_user()` when a new user signs up. This copies the sign-up metadata (name, role, phone) from Supabase Auth into our public `users` database table.

## Folder Directory Structure
- **app/src/main/java/com/example/propmanager/**
  - **data/**
    - **di/**: Hilt modules providing the SupabaseClient instance.
    - **model/**: Serialization data classes.
    - **DataRepository.kt**: Core repository interface definition.
    - **SupabaseDataRepository.kt**: Repository implementation utilizing Supabase APIs.
  - **theme/**: UI theme colors, font families, and shape systems.
  - **ui/**: ViewModels and screens grouped by feature area (auth, admin, landlord, resident, and common components).
  - **util/**: Basic helper functions like currency formatters and input validators.
  - **MainActivity.kt**: App launcher setup.
  - **Navigation.kt**: NavDisplay routes configuration.
- **supabase.sql**: PostgreSQL database schema script.
- **build.gradle.kts**: Project build scripts.

## How to Set Up and Run the Project

### Prerequisites
- Android Studio (Koala or higher)
- Java Development Kit (JDK) 17
- A Supabase account

### 1. Database Setup
1. Log into your Supabase Dashboard and go to the SQL Editor.
2. Create a new query, paste the contents of `supabase.sql`, and click **Run**.
3. This will create all tables, trigger functions, enums, and RLS policies.

### 2. Configure Credentials
1. Go to your Supabase Project Settings and copy the Project URL and API anon key.
2. Open the Kotlin file `app/src/main/java/com/example/propmanager/data/di/AppModule.kt`.
3. Put your project URL and anon key into the constant fields:
   - `SUPABASE_URL` = "YOUR_SUPABASE_PROJECT_URL"
   - `SUPABASE_KEY` = "YOUR_SUPABASE_ANON_PUBLIC_KEY"

### 3. Build & Install
1. Open the project in Android Studio.
2. Let Gradle sync and download libraries.
3. Build and install the app on an emulator or physical device by clicking the run button in the IDE, or execute this command in the terminal:
   ```powershell
   .\gradlew installDebug
   ```

## Testing and Verification
- **Sign Up Sync:** Register a new account inside the app. Confirm the profile immediately appears in your Supabase `users` database table.
- **Data Partition Test:** Create a property under one landlord account, then log in under a different landlord account to verify that the property list is empty and completely private.
- **Rent Flow Test:** As a landlord, generate a rent invoice for a tenant. Log in as that tenant to view the bill, click "Pay Now", fill out the simulated checkout form, and submit. Check both profiles to verify that the invoice is now marked as "PAID" instantly.
- **Maintenance Stream:** Submit a repair request from a tenant's account. Log in as their landlord to accept and resolve the request, and check that status changes are updated in real-time.

## Project Deliverables
- Compiling project source code.
- Setup script `supabase.sql` for Postgres database tables and RLS security.
- Comprehensive readme file documenting project stack, setup steps, and security model.
