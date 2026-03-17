package com.github.junhee8649.cleancalendar.di

import com.github.junhee8649.cleancalendar.BuildConfig
import com.github.junhee8649.cleancalendar.data.DefaultMaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.DefaultSchoolRepository
import com.github.junhee8649.cleancalendar.data.MaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.calendar.CalendarViewModel
import com.github.junhee8649.cleancalendar.schooldetail.SchoolDetailViewModel
import com.github.junhee8649.cleancalendar.schools.SchoolsViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val module = module {
        single {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
            }
        }

        single<SchoolRepository> { DefaultSchoolRepository(get()) }
        single<MaintenanceTaskRepository> { DefaultMaintenanceTaskRepository(get()) }

        viewModel { CalendarViewModel(get(), get()) }
        viewModel { SchoolsViewModel(get()) }
        viewModel { SchoolDetailViewModel(get(), get()) }
    }
}
