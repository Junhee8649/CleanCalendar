package com.github.junhee8649.cleancalendar.di

import com.github.junhee8649.cleancalendar.data.DefaultMaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.DefaultSchoolRepository
import com.github.junhee8649.cleancalendar.data.MaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.schooldetail.SchoolDetailViewModel
import com.github.junhee8649.cleancalendar.schools.SchoolsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val module = module {
        single<SchoolRepository> { DefaultSchoolRepository() }
        single<MaintenanceTaskRepository> { DefaultMaintenanceTaskRepository() }

        viewModel { SchoolsViewModel(get()) }
        viewModel { SchoolDetailViewModel(get(), get()) }
    }
}
