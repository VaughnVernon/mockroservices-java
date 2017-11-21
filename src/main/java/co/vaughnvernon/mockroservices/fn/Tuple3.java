//   Copyright © 2017 Vaughn Vernon. All rights reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package co.vaughnvernon.mockroservices.fn;

public class Tuple3<A, B, C> {
  public final A _1;
  public final B _2;
  public final C _3;
  
  public static <A, B, C> Tuple3<A, B, C> from(final A a, final B b, final C c) {
    return new Tuple3<A, B, C>(a, b, c);
  }

  private Tuple3(final A a, final B b, final C c) {
    this._1 = a;
    this._2 = b;
    this._3 = c;
  }
}
