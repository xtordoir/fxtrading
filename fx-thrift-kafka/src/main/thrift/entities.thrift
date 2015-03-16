namespace java com.impactopia.oanda.thrift

struct TPrice {
	1: string instrument,
	2: i64 timestamp,
	3: double bid,
	4: double ask
}

struct THeartbeat {
	1: i64 timestamp	
}


struct TOvershootTrader {
	1: double exposure,
	2: TPrice tickMax,
	3: bool active,
	4: double scale,
	5: TPrice tick0,
	6: double p0,
	7: double stopOS
}