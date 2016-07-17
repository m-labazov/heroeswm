package ua.home.heroeswm.market.model

import java.time.LocalTime

/**
  * Created by Maksym on 6/4/2016.
  */
class MarketItem(val itemType: String,
                 val price: Int,
                 val currentDurability: Int,
                 val maxDurability: Int,
                 val name: String,
                 val lotId: Int,
                 val time: LocalTime) {

}
