package com.impactopia.hff
import org.joda.time.DateTime

case class Tick(time: DateTime, bid: Double, ask: Double) {
	def price = (bid + ask)/2
}