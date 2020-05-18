package com.example.bricklist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedOutputStream
import java.io.FileOutputStream

class InventoryPartsXmlSerializer {
    private val serializer = XmlPullParserFactory.newInstance().newSerializer()
    private val namespace: String? = null

    suspend fun serialize(parts: List<InventoryPartWithReferences>, condition: Condition,
                          file: String) = withContext(Dispatchers.IO) {
        val stream = BufferedOutputStream(FileOutputStream(file))
        serializer.setOutput(stream, "UTF-8")
        write(parts, condition)
    }

    private fun write(parts: List<InventoryPartWithReferences>, condition: Condition) {
        serializer.startDocument("UTF-8", true)
        writeInventory(parts, condition)
        serializer.endDocument()
    }

    private fun writeInventory(parts: List<InventoryPartWithReferences>, condition: Condition) {
        serializer.startTag(namespace, Tag.INVENTORY)
        for (part in parts) {
            writePart(part, condition)
        }
        serializer.endTag(namespace, Tag.INVENTORY)
    }

    private fun writePart(part: InventoryPartWithReferences, condition: Condition) {

        val needed = part.inventoryPart.let {
            it.quantityInSet - it.quantityInStore
        }

        if (needed > 0) {
            serializer.startTag(namespace, Tag.ITEM)

            writeTag(Tag.ITEMTYPE, part.itemType.code)
            writeTag(Tag.ITEMID, part.item.code)
            writeTag(Tag.COLOR, part.color.code.toString())
            writeTag(Tag.QUANTITY_FILLED, needed.toString())

            if (condition != Condition.NOT_IMPORTANT) {
                writeTag(Tag.CONDITION, condition.value)
            }

            serializer.endTag(namespace, Tag.ITEM)
        }
    }

    private fun writeTag(tag: String, value: String) {
        serializer.startTag(namespace, tag)
        serializer.text(value)
        serializer.endTag(namespace, tag)
    }

    enum class Condition(val value: String) {
        NEW("N"), USED("U"), NOT_IMPORTANT("")
    }

    object Tag {
        const val INVENTORY = "INVENTORY"
        const val ITEM = "ITEM"
        const val ITEMTYPE = "ITEMTYPE"
        const val ITEMID = "ITEMID"
        const val COLOR = "COLOR"
        const val QUANTITY_FILLED = "QTYFILLED"
        const val CONDITION = "CONDITION"
    }
}
