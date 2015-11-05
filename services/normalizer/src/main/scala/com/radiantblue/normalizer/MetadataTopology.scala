package com.radiantblue.normalizer

import mapper._

object MetadataTopology {
  def main(args: Array[String]): Unit = {
    System.setProperty("org.geotools.referencing.forceXY", "true");

    val uploads = Kafka.spoutForTopic("uploads", UploadScheme)
    val metadataSink = Kafka.boltForTopic("metadata", DirectTupleMapper)

    val builder = new backtype.storm.topology.TopologyBuilder
    builder.setSpout("uploads", uploads)
    builder.setBolt("metadata", Inspect.bolt).shuffleGrouping("uploads")
    builder.setBolt("geotiff-metadata", InspectGeoTiff.bolt).shuffleGrouping("uploads")
    builder.setBolt("zipped-shapefile-metadata", InspectZippedShapefile.bolt).shuffleGrouping("uploads")
    builder.setBolt("publish", metadataSink)
      .shuffleGrouping("metadata")
      .shuffleGrouping("geotiff-metadata")
      .shuffleGrouping("zipped-shapefile-metadata")

    val conf = Kafka.topologyConfig
    backtype.storm.StormSubmitter.submitTopology("Metadata", conf, builder.createTopology)
  }
}