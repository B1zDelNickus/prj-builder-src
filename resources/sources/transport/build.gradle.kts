val spectrum_legacy_version = property("spectrum-version") as String

dependencies {
    implementation("codes.spectrum.bus:bus-core-lib:$spectrum_legacy_version")
    implementation("codes.spectrum:spectrum-amqp:$spectrum_legacy_version")
}