/* Soot - a J*va Optimization Framework
 * Copyright (C) 2012 Tata Consultancy Services & Ecole Polytechnique de Montreal
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.rtaclassload;

import java.util.List;
import java.util.ArrayList;

public class ListMethodTester implements MethodTester {

  private List<String> signatures;

  public ListMethodTester(){
    signatures = new ArrayList<String>();
  }

  public void addSignature(String signature){
    signatures.add(signature);
  }

  public boolean matches(RTAMethod sm){
    String testing_sig = sm.getSignature().toString();
    for(String signature : signatures){
      if(testing_sig.equals(signature)){
        return true;
      }
    }
    return false;
  }
}
