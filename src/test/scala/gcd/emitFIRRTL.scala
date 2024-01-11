package gcd

object emitFIRRTL extends App {
  (new chisel3.stage.ChiselStage()).emitFirrtl(
    new gcd.DecoupledGcd(16),
    Array("--target-dir", "generated")
  )
}