// Dichiara i plugin condivisi usando gli alias del catalogo gradle/libs.versions.toml.
// apply false li rende disponibili ai moduli senza applicarli al progetto radice.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}