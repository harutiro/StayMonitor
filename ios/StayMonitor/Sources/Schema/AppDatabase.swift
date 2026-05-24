import SQLiteData

public enum AppDatabase {
    public static func registerMigrations(_ migrator: inout DatabaseMigrator) {
        migrator.registerMigration("Create geofencePins and stayLogs tables") { db in
            try createTables(db)
            try createIndexes(db)
        }
    }

    private static func createTables(_ db: Database) throws {
        try #sql(
            """
            CREATE TABLE "geofencePins" (
              "id" TEXT PRIMARY KEY NOT NULL ON CONFLICT REPLACE DEFAULT (uuid()),
              "name" TEXT NOT NULL,
              "latitude" REAL NOT NULL,
              "longitude" REAL NOT NULL,
              "radiusMeters" REAL NOT NULL DEFAULT 100,
              "createdAt" TEXT NOT NULL
            ) STRICT
            """
        )
        .execute(db)

        try #sql(
            """
            CREATE TABLE "stayLogs" (
              "id" TEXT PRIMARY KEY NOT NULL ON CONFLICT REPLACE DEFAULT (uuid()),
              "pinID" TEXT NOT NULL,
              "pinName" TEXT NOT NULL,
              "latitude" REAL NOT NULL,
              "longitude" REAL NOT NULL,
              "enteredAt" TEXT NOT NULL,
              "exitedAt" TEXT
            ) STRICT
            """
        )
        .execute(db)
    }

    /// 滞在中ログ検索(pinID+exitedAt)と履歴表示(enteredAt)を高速化するためのインデックス。
    private static func createIndexes(_ db: Database) throws {
        try #sql(
            """
            CREATE INDEX "idx_geofencePins_createdAt" ON "geofencePins" ("createdAt")
            """
        )
        .execute(db)

        try #sql(
            """
            CREATE INDEX "idx_stayLogs_enteredAt" ON "stayLogs" ("enteredAt")
            """
        )
        .execute(db)

        try #sql(
            """
            CREATE INDEX "idx_stayLogs_pinID_exitedAt" ON "stayLogs" ("pinID", "exitedAt")
            """
        )
        .execute(db)
    }
}
