package com.example.bricklist

import java.util.*

class Inventory(val parts: List<InventoryPart>) {
    private var id = 0
    var name = ""
    private var active = true
    private var lastAccessed = Calendar.getInstance()
}