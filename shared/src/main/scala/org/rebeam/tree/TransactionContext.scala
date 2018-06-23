package org.rebeam.tree

import io.circe.generic.JsonCodec

/**
  * Provides the context within which an individual run of a DeltaIO can
  * occur. Currently just the moment in which we should run, but may be extended
  * in future.
  *
  * @param moment The moment in which this DeltaIO is running.
  *  When run on the client this is provisional,
  *  on the server it is authoritative - the authoritative
  *  value is returned back to the client when the delta
  *  is applied on the server, so the client can rerun
  *  with the correct moment.)
  */
@JsonCodec
case class TransactionContext(moment: Moment)
