package com.example.hexaward.di

import com.example.hexaward.data.analyzer.DefaultBehaviorAnalyzer
import com.example.hexaward.data.monitor.*
import com.example.hexaward.domain.analyzer.BehaviorAnalyzer
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindBehaviorAnalyzer(analyzer: DefaultBehaviorAnalyzer): BehaviorAnalyzer

    @Binds
    @IntoSet
    abstract fun bindFileActivityMonitor(monitor: FileActivityMonitor): SignalMonitor

    @Binds
    @IntoSet
    abstract fun bindPermissionWatchdog(monitor: PermissionWatchdog): SignalMonitor

    @Binds
    @IntoSet
    abstract fun bindResourceUsageMonitor(monitor: ResourceUsageMonitor): SignalMonitor

    @Binds
    @IntoSet
    abstract fun bindBackgroundExecutionMonitor(monitor: BackgroundExecutionMonitor): SignalMonitor

    @Binds
    @IntoSet
    abstract fun bindNetworkObserver(monitor: NetworkObserver): SignalMonitor

    @Binds
    @IntoSet
    abstract fun bindHoneyfileMonitor(monitor: HoneyfileMonitor): SignalMonitor
}
