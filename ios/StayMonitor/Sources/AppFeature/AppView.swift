import ComposableArchitecture
import SwiftUI

public struct AppView: View {
    let store: StoreOf<AppFeature>

    public init(store: StoreOf<AppFeature>) {
        self.store = store
    }

    public var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Image(systemName: "mappin.and.ellipse")
                    .font(.largeTitle)
                Text("StayMonitor")
                    .font(.title2)
            }
            .navigationTitle("StayMonitor")
            .onAppear { store.send(.onAppear) }
        }
    }
}

#Preview {
    AppView(
        store: Store(initialState: AppFeature.State()) {
            AppFeature()
        }
    )
}
