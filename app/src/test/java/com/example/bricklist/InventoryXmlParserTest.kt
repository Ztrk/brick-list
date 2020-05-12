package com.example.bricklist

import org.junit.Test

import org.junit.Assert.*

class InventoryXmlParserTest {
    @Test
    fun parseTest() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <INVENTORY>
               <ITEM>
                  <ITEMTYPE>M</ITEMTYPE>
                  <ITEMID>old012</ITEMID>
                  <QTY>1</QTY>
                  <COLOR>0</COLOR>
                  <EXTRA>N</EXTRA>
                  <ALTERNATE>N</ALTERNATE>
                  <MATCHID>0</MATCHID>
                  <COUNTERPART>N</COUNTERPART>
               </ITEM>
               <ITEM>
                  <ITEMTYPE>P</ITEMTYPE>
                  <ITEMID>3430c02</ITEMID>
                  <QTY>1</QTY>
                  <COLOR>11</COLOR>
                  <EXTRA>N</EXTRA>
                  <ALTERNATE>Y</ALTERNATE>
                  <MATCHID>0</MATCHID>
                  <COUNTERPART>N</COUNTERPART>
               </ITEM>
               <ITEM>
                  <ITEMTYPE>P</ITEMTYPE>
                  <ITEMID>3001old</ITEMID>
                  <QTY>3</QTY>
                  <COLOR>7</COLOR>
                  <EXTRA>N</EXTRA>
                  <ALTERNATE>N</ALTERNATE>
                  <MATCHID>0</MATCHID>
                  <COUNTERPART>N</COUNTERPART>
               </ITEM>
            </INVENTORY>
        """.trimIndent()

        val parser = InventoryXmlParser()
        val inventory = parser.parse(xml)
        assertEquals(2, inventory.parts.size)

        val part1 = InventoryPartWithReferences(
            InventoryPart(quantityInSet = 1, alternate = "N", extra = "N"),
            Item(code = "old012"),
            Color(code = 0),
            ItemType(code = "M")
        )

        val part2 = InventoryPartWithReferences(
            InventoryPart(quantityInSet = 3, alternate = "N", extra = "N"),
            Item(code = "3001old"),
            Color(code = 7),
            ItemType(code = "P")
        )

        assertEquals(part1, inventory.parts[0])
        assertEquals(part2, inventory.parts[1])
    }
}