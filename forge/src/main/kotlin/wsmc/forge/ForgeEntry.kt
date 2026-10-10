package wsmc.forge

import net.minecraftforge.fml.common.Mod
import wsmc.WSMC

@Mod(WSMC.MODID)
class ForgeEntry {
    init {
        if (instance == null) {
            instance = this
        } else {
            throw RuntimeException("Duplicated Class Instantiation: wsmc.forge.ForgeEntry")
        }
    }

    companion object {
        @JvmField
        var instance: ForgeEntry? = null
    }
}
