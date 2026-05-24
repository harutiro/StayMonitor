//
//  ios_stay_monitorApp.swift
//  ios-stay-monitor
//
//  Created by 上條栞汰 on 2026/05/21.
//

import AppFeature
import ComposableArchitecture
import SwiftUI

@main
struct StayMonitorApp: App {
    let store = Store(initialState: AppFeature.State()) {
        AppFeature()
    }

    var body: some Scene {
        WindowGroup {
            AppView(store: store)
        }
    }
}
