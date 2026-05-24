//
//  ios_stay_monitorApp.swift
//  ios-stay-monitor
//
//  Created by 上條栞汰 on 2026/05/21.
//

import AppFeature
import ComposableArchitecture
import Dependencies
import SwiftUI

@main
struct StayMonitorApp: App {
    let store: StoreOf<AppFeature>

    init() {
        do {
            try prepareDependencies {
                try $0.bootstrapDatabase()
            }
        } catch {
            fatalError("データベースの初期化に失敗しました: \(error)")
        }
        store = Store(initialState: AppFeature.State()) {
            AppFeature()
        }
    }

    var body: some Scene {
        WindowGroup {
            AppView(store: store)
        }
    }
}
