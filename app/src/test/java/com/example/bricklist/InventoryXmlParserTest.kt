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

        val part1 = InventoryPart()
        part1.typeCode = "M"
        part1.itemCode = "old012"
        part1.quantityInSet = 1
        part1.colorCode = "0"
        part1.extra = "N"
        part1.alternate = "N"

        val part2 = InventoryPart()
        part2.typeCode = "P"
        part2.itemCode = "3001old"
        part2.quantityInSet = 3
        part2.colorCode = "7"
        part2.extra = "N"
        part2.alternate = "N"

        assertPartEquals(part1, inventory.parts[0])
        assertPartEquals(part2, inventory.parts[1])
    }

    private fun assertPartEquals(expected: InventoryPart, actual: InventoryPart) {
        assertEquals(expected.typeCode, actual.typeCode)
        assertEquals(expected.itemCode, actual.itemCode)
        assertEquals(expected.quantityInSet, actual.quantityInSet)
        assertEquals(expected.colorCode, actual.colorCode)
        assertEquals(expected.extra, actual.extra)
        assertEquals(expected.alternate, actual.alternate)
    }
}