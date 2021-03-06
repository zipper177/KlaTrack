package com.g0kla.track.model;

import java.util.List;
import org.joda.time.DateTime;
import uk.me.g4dpz.satellite.TLE;
import uk.me.g4dpz.satellite.SatPos;

/**
 * 
 * @author g0kla@arrl.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 * Sat Positions is a circular array of calculated positions.  It is designed to hold enough data to fill the display
 * This is a circular buffer with a pointer for the current time.  We know the time slice period so we can
 * calculate the time for every other slot.  The data in the slots does not change, but the current time
 * pointer marches forward.  We purge the old data, keeping only a certain history
 * 
 * @author chris
 *
 */
public class SatPositions {
	int calcFreq; // the period in seconds between calculations
	int oldPeriod; // number of mins after current time that we keep positions for
	int forecastPeriod; // number of mins into the future we calculate positions for
	int numberOfSamples; // the number of position samples to store - should equal the length of the satPositions queue

	private SatPos[][] satPositionQueue; // the circular buffer, which is a buffer full of lists of sat positions
	private int nowPointer; // points to the current timeslice
	private int endPointer; // the end of the data, we add data at this position + 1
	private int startPointer; // position zero in the circular buffer
	List<TLE> TLEs;
	DateTime now;
	
	public SatPositions(List<TLE> TLEs, int oldPeriod, int forecastPeriod, int calcFreq) {
		this.TLEs = TLEs;
		this.calcFreq = calcFreq;
		this.oldPeriod = oldPeriod;
		this.forecastPeriod = forecastPeriod;
		this.numberOfSamples = (60*oldPeriod + 60*forecastPeriod) / calcFreq;
		satPositionQueue = new SatPos[numberOfSamples][];
		nowPointer = 60*oldPeriod/calcFreq + 1;
		endPointer = 0; // special starting position - we need to fill the buffer
		startPointer = 0; // special starting position - we need to fill the buffer
	}
	
	public String getSatName(int num) { return TLEs.get(num).getName(); }
	
	public int getNumberOfSats() { return TLEs.size(); }
	public int getNumberOfSamples() { return numberOfSamples; }
	
	public int getNowPointer() {
		return 60*oldPeriod/calcFreq + 1;
	}
	
	private int incPointer(int p) {
		p++;
		if (p >= numberOfSamples)
			p = 0;
		return p;
	}
	
	public void incNow() {
		startPointer = incPointer(startPointer);
		now = now.plusSeconds(calcFreq);
	}
		
	public void setNow(DateTime now) {
		this.now = now;
	}
	public DateTime getNow() { return now; }
	public int getCalcFreq() { return calcFreq; }
	public int getPastPeriod() { return oldPeriod; }
	public int getForecastPeriod() { return forecastPeriod; }
	
	/**
	 * Return the UTC date for the given timeslice.  Useful for plotting the graph
	 * The now time is the time that the data was last updated
	 * @param timeSlice
	 * @return
	 */
	public DateTime getDateFromTimeSlice(int timeSlice) {
		
		return now;
	}
	
	public SatPos[] get(int timeSlice) {
		int offset = (startPointer + timeSlice) % numberOfSamples;
		return satPositionQueue[offset];
	}
	
	public void add(SatPos[] satPositionList) {
		satPositionQueue[endPointer] = satPositionList;
		endPointer = incPointer(endPointer);
	}
	
	public void add(int timeSlice, SatPos[] satPositionList) {
		satPositionQueue[timeSlice] = satPositionList;
	}	
}
