import Foundation
import SQLiteData
import StructuredQueries

/// 地図上に登録するピン。中心座標から半径 `radiusMeters` の Geofence を構成する。
@Table
public struct GeofencePin: Identifiable, Hashable, Sendable {
    public let id: UUID
    public var name: String
    public var latitude: Double
    public var longitude: Double
    public var radiusMeters: Double
    public var createdAt: Date

    public init(
        id: UUID,
        name: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 100,
        createdAt: Date
    ) {
        self.id = id
        self.name = name
        self.latitude = latitude
        self.longitude = longitude
        self.radiusMeters = radiusMeters
        self.createdAt = createdAt
    }
}
