package com.example.bricklist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.lang.IllegalStateException

class InventoryXmlParser {
    private val namespace: String? = null

    suspend fun parse(xml: String) = withContext(Dispatchers.Default) {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        val reader = StringReader(xml)
        parser.setInput(reader)
        // StringReader cannot block, as it reads data from memory
        @Suppress("BlockingMethodInNonBlockingContext")
        parser.nextTag()
        readInventory(parser)
    }

    private fun readInventory(parser: XmlPullParser): InventoryWithParts {
        parser.require(XmlPullParser.START_TAG, namespace, "INVENTORY")

        val inventoryParts = mutableListOf<InventoryPartWithReferences>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if (parser.name == "ITEM") {
                    val part = readInventoryPart(parser)
                    if (part.inventoryPart.alternate == "N")
                        inventoryParts.add(part)
                }
                else {
                    skip(parser)
                }
            }
        }
        return InventoryWithParts(parts = inventoryParts)
    }

    private fun readInventoryPart(parser: XmlPullParser): InventoryPartWithReferences {
        parser.require(XmlPullParser.START_TAG, namespace, "ITEM")
        val inventoryPart = InventoryPartWithReferences()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "ITEMTYPE" ->
                    inventoryPart.itemType.code = readText(parser, "ITEMTYPE")
                "ITEMID" ->
                    inventoryPart.item.code = readText(parser, "ITEMID")
                "QTY" ->
                    inventoryPart.inventoryPart.quantityInSet = readText(parser, "QTY").toInt()
                "COLOR" ->
                    inventoryPart.color.code = readText(parser, "COLOR").toInt()
                "EXTRA" ->
                    inventoryPart.inventoryPart.extra =
                        if (readText(parser, "EXTRA") == "Y") 1 else 0
                "ALTERNATE" ->
                    inventoryPart.inventoryPart.alternate = readText(parser, "ALTERNATE")
                else -> skip(parser)
            }
        }
        return inventoryPart
    }

    private fun readText(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, namespace, tag)
        val text = parser.nextText()
        parser.require(XmlPullParser.END_TAG, namespace, tag)
        return text
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> ++depth
                XmlPullParser.END_TAG -> --depth
            }
        }
    }
}