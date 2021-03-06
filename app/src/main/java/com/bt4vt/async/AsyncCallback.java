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

package com.bt4vt.async;

/**
 * Calls back the async requester.
 *
 * @author Ben Sechrist
 */
public interface AsyncCallback<T> {

  /**
   * The async task was completed successfully.
   * @param t the result of the async task
   */
  void onSuccess(T t);

  /**
   * The async task threw an exception.
   * @param e the exception
   */
  void onException(Exception e);
}
