import AppFeature
import Dependencies
import DependenciesTestSupport
import Entity
import Foundation
import SQLiteData
import Testing

@Suite(.dependencies { try $0.bootstrapDatabase() })
struct AppDatabaseTests {
    @Test
    func createsSchemaAndRoundTripsRecords() async throws {
        @Dependency(\.defaultDatabase) var database

        let pinID = UUID(0)
        try await database.write { db in
            try db.seed {
                GeofencePin(
                    id: pinID,
                    name: "自宅",
                    latitude: 35.681,
                    longitude: 139.767,
                    createdAt: Date(timeIntervalSince1970: 0)
                )
                StayLog(
                    id: UUID(1),
                    pinID: pinID,
                    pinName: "自宅",
                    latitude: 35.681,
                    longitude: 139.767,
                    enteredAt: Date(timeIntervalSince1970: 100)
                )
            }
        }

        let pins = try await database.read { db in try GeofencePin.all.fetchAll(db) }
        let logs = try await database.read { db in try StayLog.all.fetchAll(db) }

        #expect(pins.count == 1)
        #expect(pins.first?.name == "自宅")
        #expect(pins.first?.radiusMeters == 100)
        #expect(logs.count == 1)
        #expect(logs.first?.isStaying == true)
    }
}
