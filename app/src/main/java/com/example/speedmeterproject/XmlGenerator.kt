package com.example.speedmeterproject

import android.util.Xml
import java.io.StringWriter

class XmlGenerator {

    /**
     * Return a string formatted in .tcx file format, based on given trackpointList
     */
    fun generateTCX(trackpointList : List<Trackpoint>, activityID : String, timeAtStart : Long, timeAtStop : Long, distance : Double) : String {
        val xmlSerializer = Xml.newSerializer()
        val writer = StringWriter()

        /**
         * Prepare .tcx file content
         */
        xmlSerializer.apply {
            setOutput(writer)
            startDocument("UTF-8", true)
            startTag("", "TrainingCenterDatabase")
            attribute("", "xsi:schemaLocation", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd")
            attribute("", "xmlns:ns5", "http://www.garmin.com/xmlschemas/ActivityGoals/v1")
            attribute("", "xmlns:ns3", "http://www.garmin.com/xmlschemas/ActivityExtension/v2")
            attribute("", "xmlns:ns2", "http://www.garmin.com/xmlschemas/UserProfile/v2")
            attribute("", "xmlns", "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2")
            attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            attribute("", "xmlns:ns4", "http://www.garmin.com/xmlschemas/ProfileExtension/v1")
                startTag("", "Activities")
                    startTag("", "Activity")
                    attribute("", "Sport", "Biking")
                    startTag("", "Id")
                        text(activityID)
                    endTag("", "Id")
                    startTag("", "Lap")
                    attribute("", "StartTime", activityID)
                        startTag("", "TotalTimeSeconds")
                            text(((timeAtStop - timeAtStart)/1000.0).toString())
                        endTag("", "TotalTimeSeconds")
                        startTag("", "DistanceMeters")
                            text((distance*1000.0).toString())
                        endTag("", "DistanceMeters")
                        startTag("", "Intensity")
                            text("Active")
                        endTag("", "Intensity")
                        startTag("", "TriggerMethod")
                            text("Manual")
                        endTag("", "TriggerMethod")
                        startTag("", "Track")
                            for(trackpoint in trackpointList) {
                                startTag("", "Trackpoint")
                                startTag("", "Time")
                                text(trackpoint.time)
                                endTag("", "Time")
                                startTag("", "DistanceMeters")
                                text((trackpoint.distance*1000.0).toString())
                                endTag("", "DistanceMeters")
                                endTag("", "Trackpoint")
                            }
                        endTag("", "Track")
                    endTag("", "Lap")
                    endTag("", "Activity")
                endTag("", "Activities")
            endTag("", "TrainingCenterDatabase")
            endDocument()
        }

        return writer.toString()

    }

}
