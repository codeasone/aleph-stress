{:store :database
 :migration-table-name "schema_migrations"
 :db {:classname   "org.postgresql.Driver"
      :subprotocol "postgresql"
      :subname     "//localhost:5432/aleph_stress"
      :user        "postgres"
      :password    "insecure"}}
