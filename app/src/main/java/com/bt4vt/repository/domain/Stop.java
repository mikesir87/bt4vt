/*
 * Copyright 2015 Ben Sechrist
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

package com.bt4vt.repository.domain;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Transit stop.
 *
 * @author Ben Sechrist
 */
@Root(name = "ScheduledStops")
public class Stop {

  @Element(name = "StopName")
  private String name;

  @Element(name = "StopCode")
  private Integer code;

  @Element(name = "Latitude")
  private Double latitude;

  @Element(name = "Longitude")
  private Double longitude;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  @Override
  public int hashCode() {
    if (getCode() != null) {
      return getCode().hashCode();
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Stop)) return false;
    Stop that = (Stop) o;
    return !(this.getCode() == null || that.getCode() == null) && this.getCode().equals(that.getCode());
  }

  @Override
  public String toString() {
    return String.format("(%s - %s)", getName(), getCode());
  }
}
