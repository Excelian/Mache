package com.excelian.mache.core;

/**
 * Enum to denote what should be done with the schema created in the underlying
 * datastore on startup and shutdown.
 */
public enum SchemaOptions {
    CREATESCHEMAIFNEEDED(true, false),
    USEEXISTINGSCHEMA(false, false),
    CREATEANDDROPSCHEMA(true, true);

    private boolean createSchema;
    private boolean dropSchema;

    SchemaOptions(boolean createSchema, boolean dropSchema) {
        this.createSchema = createSchema;

        this.dropSchema = dropSchema;
    }

    public boolean shouldCreateSchema() {
        return createSchema;
    }

    public boolean shouldDropSchema() {
        return dropSchema;
    }
}
