// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Iterator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    SortedSet<TimeRange> filteredTimeRanges = filterEvents(events, request);
    int duration = new Long(request.getDuration()).intValue();
    Collection<TimeRange> allPossibleIntervals = emptyIntervals(filteredTimeRanges, duration);
    
    return allPossibleIntervals;

  }
  // If the event has at least one common attendee as the request
  // return true. Otherwise return false
  public boolean needsAttendeeFromOtherEvent(Event event, MeetingRequest request) {
      for (String attendee : request.getAttendees()) {
          if (event.getAttendees().contains(attendee)) {
              return true;
          }
      }

      return false;
  }

  // Filter from all the events just the ones that have at least one
  // attendee from the event we are trying to build (the remaing do 
  // not influence our choice)
  public SortedSet<TimeRange> filterEvents(Collection<Event> events, MeetingRequest request) {
      // Builds a set of timeRanges sorted by the start
      // Reduce time complexity to build it as we find elements, than to
      // sort it afterwards 
      TreeSet<TimeRange> filteredTimeRanges = new TreeSet<>(TimeRange.ORDER_BY_START);

      for (Event event : events) {
          if (needsAttendeeFromOtherEvent(event, request)) {
              filteredTimeRanges.add(event.getWhen());
          }
      }

      return filteredTimeRanges;
  }

  // Finds all empty intervals in which we can place a new
  // calendar invite. It uses a greedy approach. Sort all 
  // the events by the start time and whenever it finds a
  // start that is bigger than the last biggest end, it means that
  // we have an empty interval. To add the interval to 
  // allEmptyIntervals, it has to have a duration at least as big 
  // as our request 
  public Collection<TimeRange> emptyIntervals(SortedSet<TimeRange> events, int duration) {
      ArrayList<TimeRange> allEmptyIntervals = new ArrayList<>();
      
      // If there are no constraints, we can add the whole day
      if (events.isEmpty()) {
          if (TimeRange.WHOLE_DAY.duration() >= duration)
            allEmptyIntervals.add(TimeRange.WHOLE_DAY);
          return allEmptyIntervals;
      }

      Iterator<TimeRange> iterator = events.iterator();
      TimeRange current = iterator.next();

      int end = current.end();
      int start = current.start();

      if (start > TimeRange.START_OF_DAY)
        allEmptyIntervals.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, start, false));

      while(iterator.hasNext()) {
        current = iterator.next();
        if(end < current.start()) {
          // We have found an empty interval
          TimeRange newTimeRange = TimeRange.fromStartEnd(end, current.start(), false);
          if (newTimeRange.duration() >= duration) {
            allEmptyIntervals.add(newTimeRange);
          }
          
          end = current.end();
        } else if (end < current.end()) {
          // Update the last biggest end
          end = current.end();
        } 
      }

      // Add the remaining of the day
      if (end < TimeRange.END_OF_DAY) {
        TimeRange newTimeRange = TimeRange.fromStartEnd(end , TimeRange.END_OF_DAY, true);
        if (newTimeRange.duration() >= duration) {
          allEmptyIntervals.add(newTimeRange);
        }
      }

      return allEmptyIntervals;
  }
}
