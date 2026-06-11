package com.example.propmanager.data.di

import com.example.propmanager.data.DataRepository
import com.example.propmanager.data.SupabaseDataRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataRepository(
        supabaseDataRepository: SupabaseDataRepository
    ): DataRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val SUPABASE_URL = "https://qdbqadwofrmttijlzfxf.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkYnFhZHdvZnJtdHRpamx6ZnhmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODExMDkxODEsImV4cCI6MjA5NjY4NTE4MX0._odoy1AAcwecx645YREdASgJNHsSta_rq9F5HVfzW0Y"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}

