import Testing

@testable import AppFeature

@Suite
struct AppFeatureTests {
    @Test
    func appFeatureInitialStateIsEquatable() {
        let state = AppFeature.State()
        #expect(state == AppFeature.State())
    }
}
