package com.example.bricklist

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.lang.IllegalStateException

class InventoryXmlParser {
    private val namespace: String? = null

    fun parse(xml: String): Inventory {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        val reader = StringReader(xml)
        parser.setInput(reader)
        parser.nextTag()
        return readInventory(parser)
    }

    private fun readInventory(parser: XmlPullParser): Inventory {
        parser.require(XmlPullParser.START_TAG, namespace, "INVENTORY")

        val inventoryParts = mutableListOf<InventoryPart>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                if (parser.name == "ITEM") {
                    val part = readInventoryPart(parser)
                    if (part.alternate == "N")
                        inventoryParts.add(part)
                }
                else {
                    skip(parser)
                }
            }
        }
        return Inventory(inventoryParts)
    }

    private fun readInventoryPart(parser: XmlPullParser): InventoryPart {
        parser.require(XmlPullParser.START_TAG, namespace, "ITEM")
        val inventoryPart = InventoryPart()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue;
            }
            when (parser.name) {
                "ITEMTYPE" -> inventoryPart.typeCode = readText(parser, "ITEMTYPE")
                "ITEMID" -> inventoryPart.itemCode = readText(parser, "ITEMID")
                "QTY" -> inventoryPart.quantityInSet = readText(parser, "QTY").toInt()
                "COLOR" -> inventoryPart.colorCode = readText(parser, "COLOR")
                "EXTRA" -> inventoryPart.extra = readText(parser, "EXTRA")
                "ALTERNATE" -> inventoryPart.alternate = readText(parser, "ALTERNATE")
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
                XmlPullParser.START_TAG -> ++depth;
                XmlPullParser.END_TAG -> --depth;
            }
        }
    }
}