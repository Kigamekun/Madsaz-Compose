package id.madsaz.app.model

object DemoClassifier {
    fun classify(recordingId: String): CryCategory {
        require(recordingId.isNotBlank()) { "recordingId tidak boleh kosong" }
        // ponytail: deterministic placeholder; replace this single call with the real model.
        return CryCategory.entries[Math.floorMod(recordingId.hashCode(), CryCategory.entries.size)]
    }
}
