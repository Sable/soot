/* Soot - a J*va Optimization Framework
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

import java.util.Set;
import java.util.TreeSet;

public class MainEntryMethodTester implements EntryMethodTester {

  private Set<String> newInvokes;
  private String className;
  private boolean classNameSet;

  public MainEntryMethodTester(){
    newInvokes = new TreeSet<String>();
    classNameSet = false;
  }

  public MainEntryMethodTester(String className){
    newInvokes = new TreeSet<String>();
    this.className = className;
    classNameSet = true;
  }

  public boolean matches(RTAMethod method){
    if(method.isStatic() == false){
      return false;
    }
    if(classNameSet){
      String currClassName = method.getSignature().getClassName().toString();
      if(className.equals(currClassName) == false){
        return false;
      }
    }
    MethodSignature signature = method.getSignature();
    String subsig = signature.getSubSignatureString();
    if(subsig.equals("void main(java.lang.String[])")){
      newInvokes.add(signature.getClassName().toString());
      return true;
    }
    return false;
  }

  public Set<String> getNewInvokes(){
    return newInvokes;
  }
}
