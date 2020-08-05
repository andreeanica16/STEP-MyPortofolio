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
import java.util.Vector;
import java.util.Collections;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int duration = new Long(request.getDuration()).intValue();
    Collection<String> allAttendees = new ArrayList(request.getAttendees());
    allAttendees.addAll(request.getOptionalAttendees());



    Collection<TimeRange> includeOptionalAttendees = solveQueryforAttendees(events, allAttendees, duration);

    if (includeOptionalAttendees.isEmpty()) {
        return solveMaximumOptionalAttedees(events, request, duration);
    } else {
        return includeOptionalAttendees;
    }
  }

  public Collection<TimeRange> solveQueryforAttendees(Collection<Event> events, Collection<String> attendees, int duration) {
    Collection<TimeRange> filteredTimeRanges = filterEvents(events, attendees);
    Collection<TimeRange> allPossibleIntervals = emptyIntervals(filteredTimeRanges, duration);
    
    return allPossibleIntervals;
  }

  // If the event has at least one common attendee as the request
  // return true. Otherwise return false
  public boolean needsAttendeeFromOtherEvent(Event event, Collection<String> attendees) {
      for (String attendee : attendees) {
          if (event.getAttendees().contains(attendee)) {
              return true;
          }
      }

      return false;
  }

  // Filter from all the events just the ones that have at least one
  // attendee from the event we are trying to build (the remaing do 
  // not influence our choice)
  public Collection<TimeRange> filterEvents(Collection<Event> events, Collection<String> attendees) {
      // Builds a set of timeRanges sorted by the start
      // Reduce time complexity to build it as we find elements, than to
      // sort it afterwards 
      ArrayList<TimeRange> filteredTimeRanges = new ArrayList<>();

      for (Event event : events) {
          if (needsAttendeeFromOtherEvent(event, attendees)) {
              filteredTimeRanges.add(event.getWhen());
          }
      }

      Collections.sort(filteredTimeRanges, TimeRange.ORDER_BY_START);

      return filteredTimeRanges;
  }

  // Finds all empty intervals in which we can place a new
  // calendar invite. It uses a greedy approach. Sort all 
  // the events by the start time and whenever it finds a
  // start that is bigger than the last biggest end, it means that
  // we have an empty interval. To add the interval to 
  // allEmptyIntervals, it has to have a duration at least as big 
  // as our request 
  public Collection<TimeRange> emptyIntervals(Collection<TimeRange> events, int duration) {
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

  // Class used to keep track of the program of all optional attendees
  private class Person {
      String name;
      // busyHours[i] tells if the current person is in a meeting at the minute i
      // of a day
      int[] busyHours = new int[1442];

      Person() {
          this.name = "";
      }

      Person(String name, Collection<Event> events) {
          for (Event event : events) {
              if (event.getAttendees().contains(name)) {
                  for (int i = 0; i < event.getWhen().duration(); i++) {
                      busyHours[event.getWhen().start() +  i] = 1;
                  }
              }
          }

          this.name = name;
      }
  }
  
  // Finds the intervals when the most optional attendees can attend 
  // the meeting. Uses a dynimac programming approach.
  // dp[i][j] = how many optional attendees from the first i optional
  //            attendees can take part in a meeting starting at the 
  //            minute j
  public Collection<TimeRange> solveMaximumOptionalAttedees(Collection<Event> events, MeetingRequest request, int duration) {
      Collection<TimeRange> mandatoryIntervals = solveQueryforAttendees(events, request.getAttendees(), duration);
      // A google calendar invite has at most 200 people
      int[][] dp = new int[200][1442]; 

      // Only the intervals when mandatory attendees can attend should
      // be considered
      for (TimeRange time : mandatoryIntervals) {
          for (int i = 0; i <= time.duration() - duration; i++) {
              dp[0][i + time.start()] = 1;
          }
      }

      Vector<Person> optionalPeople = new Vector<>();
      optionalPeople.add(new Person()); // add dummy person

      for (String people : request.getOptionalAttendees()) {
          optionalPeople.add(new Person(people, events));
      } 

      for (int i = 1; i <= request.getOptionalAttendees().size(); i++) {
          for (int j = 0; j <= 1440 - duration; j++) {
              if (dp[i - 1][j] >= 1) {
                  // Makes sure it is a valid interval
                  if (canStartMeeting(optionalPeople.elementAt(i), duration, j)) {
                      // If person i can attend increment dp
                      dp[i][j] = dp[i - 1][j] + 1; 
                  } else {
                      dp[i][j] = dp[i - 1][j]; 
                  }
              }
          }
      }

      ArrayList<TimeRange> allIntervals = new ArrayList<>();
      int max = 1;
      int i = 0;
      int lastRow = request.getOptionalAttendees().size();
      while (i <= 1440 - duration) {
          if (dp[lastRow][i] >= max) {
              if (dp[lastRow][i] > max) {
                  max = dp[lastRow][i];
                  allIntervals = new ArrayList<>();
              }
              int j = i + 1;
              while (dp[lastRow][j] == dp[lastRow][i] && j <= 1440 - duration) {
                  j++;
              }
    
              allIntervals.add(TimeRange.fromStartEnd(i, j - 1 + duration, false));

              i = j;
          } else {
              i++;
          } 
      }

      return makeIntervalsCompact(allIntervals);
  }

  // Tells if a person is available for the entire meeting 
  private boolean canStartMeeting(Person person, int duration, int start) {
      for (int i = start; i < start + duration && i <= 1440; i++) {
          if (person.busyHours[i] == 1) {
              return false;
          }
      }

      return true;
  }

  // Concatenate intervals to make them compact
  private Collection<TimeRange> makeIntervalsCompact(Collection<TimeRange> intervals) {
      int begin = -1;
      int end = -1;

      ArrayList<TimeRange> finalIntervals = new ArrayList<>();

      for (TimeRange interval : intervals) {
          if (begin == -1) {
              begin = interval.start();
              end = interval.end();
          } else {
              if (end == interval.start()) {
                  end = interval.end();
              } else {
                  finalIntervals.add(TimeRange.fromStartEnd(begin, end, false));
                  begin = interval.start();
                  end = interval.end();
              }
          }
      }
      if (begin != -1 && end != -1)
        finalIntervals.add(TimeRange.fromStartEnd(begin, end, false));

      return finalIntervals; 
  }
}
