import Entity
import Foundation
import Testing

@Suite
struct StayLogTests {
    @Test
    func staysWhileNotExited() {
        let log = StayLog(
            id: UUID(),
            pinID: UUID(),
            pinName: "自宅",
            latitude: 35.681,
            longitude: 139.767,
            enteredAt: Date(timeIntervalSince1970: 0)
        )

        #expect(log.isStaying)
        #expect(log.stayDuration == nil)
    }

    @Test
    func reportsDurationAfterExit() {
        let log = StayLog(
            id: UUID(),
            pinID: UUID(),
            pinName: "自宅",
            latitude: 35.681,
            longitude: 139.767,
            enteredAt: Date(timeIntervalSince1970: 0),
            exitedAt: Date(timeIntervalSince1970: 600)
        )

        #expect(!log.isStaying)
        #expect(log.stayDuration == 600)
    }
}
