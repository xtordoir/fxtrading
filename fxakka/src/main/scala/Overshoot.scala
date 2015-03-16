trait Mode
object UpMode extends Mode {
	def reverse(): Mode = Downmode
}
object DownMode extends Mode {
	def reverse(): Mode = Upmode	
}

case class Overshoot(x: Double, xExt: Double, xRev: Double, mode: Mode, dX: Double) {
	def update(overshoot: Overshoot, x: Double): Overshoot = {
		overshoot.mode match {
			case DownMode => {
				if (x < overshoot.xExt) {
					Overshoot(x, x, overshoot.xRev, overshoot.mode, overshoot.dX)
				} else if ((x − overshoot.xExt)/overshoot.xExt >= overshoot.dX) {
					Overshoot(x, x, overshoot.xExt, overshoot.mode.reverse, overshoot.dX)
				} else {
					Overshoot(x, overshoot.xExt, overshoot.xRev, overshoot.mode, overshoot.dX)
				}
			}
			case UpMode => {
				if (x > overshoot.xExt) {
					Overshoot(x, overshoot.xRev, overshoot.mode, overshoot.dX)
				} else if ((x − overshoot.xExt)/overshoot.xExt <= -overshoot.dX) {
					Overshoot(x, x, overshoot.xExt, overshoot.mode.reverse, overshoot.dX)
				} else {
					Overshoot(x, overshoot.xExt, overshoot.xRev, overshoot.mode, overshoot.dX)
				}
			}
		}
	}
}
