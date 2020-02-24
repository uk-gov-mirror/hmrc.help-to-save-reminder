/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helptosavereminder.util

import java.time.{LocalDate, LocalDateTime, YearMonth, ZoneId}
import java.util.Calendar

object DateTimeFunctions {

  def getNextSendDate(daysToReceive: Seq[Int]): LocalDate = {

    val currentDayOfMonth = Calendar.getInstance.get(Calendar.DAY_OF_MONTH)
    val nextAvailableDayOfMonth = daysToReceive.filter(x => x > currentDayOfMonth).headOption

    val maxDaysInMonth =
      (YearMonth.of(Calendar.getInstance.get(Calendar.YEAR), Calendar.getInstance.get(Calendar.MONTH))).lengthOfMonth()

    nextAvailableDayOfMonth match {
      case Some(day) => (LocalDate.now).plusDays(day - currentDayOfMonth)
      case None      => (LocalDate.now).plusMonths(1).withDayOfMonth(daysToReceive.head)
    }

  }

  def getNextSchedule(scheduledDays: String, scheduledTimes: String) = {

    val scheduledDaysList = scheduledDays.split(",").toList
    val scheduledTimesList = scheduledTimes.split(",").toList

    val mapOfMaps = scheduledDaysList.flatMap({ day =>
      {
        scheduledTimesList.map({ timePoint =>
          (day.toInt, timePoint.split(':').toList(0), timePoint.split(':').toList(1))
        })
      }
    })

    println(mapOfMaps)

    val scheduledTimesFinalized = mapOfMaps.map(
      { x =>
        {

          (LocalDate.now)
            .plusMonths(0)
            .withDayOfMonth(x._1.toInt)
            .atStartOfDay()
            .plusHours(x._2.toInt)
            .plusMinutes(x._3.toInt)

        }
      }
    )

    val nextTimeSlot = scheduledTimesFinalized.find(x => x.isAfter(LocalDateTime.now()))

    nextTimeSlot match {
      case Some(slot) => {
        val longMilliSecs = slot.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        val currentTimeInMillis = System.currentTimeMillis()
        val delayInMillis = (longMilliSecs - currentTimeInMillis)
        println("Nexttime slot in Millis Away: " + delayInMillis)
        println("Nexttime slot in Java Format " + LocalDateTime.now().plusSeconds(delayInMillis/1000) )
      }
      case None       => {
        val timeTuples = mapOfMaps.apply(0)
        val nextMonthSlot = (LocalDate.now).plusMonths(1).withDayOfMonth(timeTuples._1).atStartOfDay().plusHours(timeTuples._2.toInt).plusMinutes(timeTuples._3.toInt)
        println("No time slot this month, nextMonthSlot = " + nextMonthSlot)
      }
    }



  }
}

object HelloWorld {
  def main(args: Array[String]): Unit = {

    println("NextSend Date for Seq(1) = " + DateTimeFunctions.getNextSendDate(Seq(1)))
    println("NextSend Date for Seq(25) = " + DateTimeFunctions.getNextSendDate(Seq(25)))
    println("NextSend Date for Seq(1,25) = " + DateTimeFunctions.getNextSendDate(Seq(1,25)))

    DateTimeFunctions.getNextSchedule("1,22", "10:30,16:30")
  }
}
