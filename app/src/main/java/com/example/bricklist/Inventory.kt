package com.example.bricklist

import java.util.*

class Inventory {
    private var id = 0
    public var name = ""
    private var active = true
    private var lastAccessed = Calendar.getInstance()
    public var parts = mutableListOf<InventoryPart>()
}