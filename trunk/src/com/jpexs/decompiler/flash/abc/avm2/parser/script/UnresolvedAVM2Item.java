/*
 * Copyright (C) 2014 JPEXS
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
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import static com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item.ins;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class UnresolvedAVM2Item extends AssignableAVM2Item {

    private String name;

    private GraphTargetItem index;
    private int nsKind = -1;
    public List<Integer> openedNamespaces;
    public int line;
    public GraphTargetItem type;
    private GraphTargetItem ns = null;
    public GraphTargetItem resolved;
    private boolean mustBeType;
    public List<String> importedClasses;
    public List<GraphTargetItem> scopeStack = new ArrayList<GraphTargetItem>();
    public List<String> subtypes;

    @Override
    public AssignableAVM2Item copy() {
        UnresolvedAVM2Item c = new UnresolvedAVM2Item(subtypes, importedClasses, mustBeType, type, line, name, assignedValue, openedNamespaces);
        c.setNs(ns);
        c.nsKind = nsKind;
        c.setIndex(index);
        c.resolved = resolved;
        return c;
    }

    public void setSlotScope(int slotScope) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setSlotScope(slotScope);
        }
    }

    public int getSlotScope() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getSlotScope();
        }
        return -1;
    }

    public void setNs(GraphTargetItem ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setRegNumber(regNumber);
        }
    }

    public int getSlotNumber() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getSlotNumber();
        }
        return -1;
    }

    public void setSlotNumber(int slotNumber) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setSlotNumber(slotNumber);
        }
    }

    public int getRegNumber() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getRegNumber();
        }
        return -1;
    }

    public GraphTargetItem getNs() {
        return ns;
    }

    public void appendName(String name) {
        this.name += "." + name;
    }

    public void setDefinition(boolean definition) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setDefinition(definition);
        }
    }

    public void setIndex(GraphTargetItem index) {
        this.index = index;
    }

    public GraphTargetItem getIndex() {
        return index;
    }

    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    public int getNsKind() {
        return nsKind;
    }

    @Override
    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public String getVariableName() {
        return name;
    }

    public UnresolvedAVM2Item(List<String> subtypes, List<String> importedClasses, boolean mustBeType, GraphTargetItem type, int line, String name, GraphTargetItem storeValue, List<Integer> openedNamespaces) {
        super(storeValue);
        this.name = name;
        this.assignedValue = storeValue;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
        this.mustBeType = mustBeType;
        this.importedClasses = importedClasses;
        this.subtypes = subtypes;
    }

    public boolean isDefinition() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).isDefinition();
        }
        return false;
    }

    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    private int allNsSet(ABC abc) {
        int nssa[] = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null);
            case "int":
                return new IntegerValueAVM2Item(null, 0L);
            case "Number":
                return new NanAVM2Item(null);
            default:
                return new NullAVM2Item(null);
        }
    }

    public static AVM2Instruction generateCoerce(SourceGenerator generator, String type) {
        AVM2Instruction ins;
        switch (type) {
            case "int":
                ins = ins(new ConvertIIns());
                break;
            case "*":
                ins = ins(new CoerceAIns());
                break;
            case "String":
                ins = ins(new CoerceSIns());
                break;
            default:
                int type_index = new TypeItem(type).resolveClass(((AVM2SourceGenerator) generator).abc);
                ins = ins(new CoerceIns(), type_index);
                break;
        }
        return ins;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (resolved == null) {
            throw new RuntimeException("Unresolved");
        }
        return resolved.toSource(localData, generator);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (resolved == null) {
            throw new RuntimeException("Unresolved");
        }
        return resolved.toSourceIgnoreReturnValue(localData, generator);
    }

    @Override
    public boolean hasReturnValue() {
        if (resolved != null) {
            return resolved.hasReturnValue();
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (resolved != null) {
            return resolved.needsSemicolon();
        }
        return false;
    }

    @Override
    public String toString() {
        if (resolved != null) {
            return resolved.toString();
        }
        return name;
    }

    @Override
    public GraphTargetItem returnType() {
        if (index != null) {
            return TypeItem.UNBOUNDED;
        }
        if (type == null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        if (resolved == null) {
            throw new RuntimeException("Unresolved");
        }
        if (resolved instanceof AssignableAVM2Item) {
            return ((AssignableAVM2Item) resolved).toSourceChange(localData, generator, post, decrement, needsReturn);
        }
        throw new RuntimeException("Cannot assign");
    }

    public GraphTargetItem resolve(List<GraphTargetItem> paramTypes, List<String> paramNames, ABC abc, List<ABC> otherAbcs, List<MethodBody> callStack, List<AssignableAVM2Item> variables) throws CompilationException {
        List<String> parts = new ArrayList<>();
        if (name.contains(".")) {
            String partsArr[] = name.split("\\.");
            for (String p : partsArr) {
                parts.add(p);
            }
        } else {
            parts.add(name);
        }

        if (ns != null) {
            if (name.contains(".")) {
                throw new CompilationException("Invalid property name", line);
            }
            resolved = new NameAVM2Item(type, line, name, assignedValue, false, openedNamespaces);
            ((NameAVM2Item) resolved).setNs(ns);
            ((NameAVM2Item) resolved).setIndex(index);
            return resolved;
        }

        if (scopeStack.isEmpty()) { //Everything is multiname property in with command

            //search for variable
            for (AssignableAVM2Item a : variables) {
                if (a instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) a;
                    if (n.isDefinition() && parts.get(0).equals(n.getVariableName())) {
                        NameAVM2Item ret = new NameAVM2Item(n.type, n.line, parts.get(0), null, false, openedNamespaces);
                        ret.setSlotScope(n.getSlotScope());
                        ret.setSlotNumber(n.getSlotNumber());
                        ret.setRegNumber(n.getRegNumber());
                        resolved = ret;
                        for (int i = 1; i < parts.size(); i++) {
                            resolved = new PropertyAVM2Item(resolved, parts.get(i), null, abc, otherAbcs, openedNamespaces, new ArrayList<MethodBody>());
                            if (i == parts.size() - 1) {
                                ((PropertyAVM2Item) resolved).index = index;
                                ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                            }
                        }
                        if (parts.size() == 1) {
                            ret.setIndex(index);
                            ret.setAssignedValue(assignedValue);
                        }
                        ret.setNs(n.getNs());
                        return ret;
                    }
                }
            }
        }
        //Search for types in imported classes
        for (String imp : importedClasses) {
            String impName = imp;
            String impPkg = "";
            if (impName.contains(".")) {
                impPkg = impName.substring(0, impName.lastIndexOf("."));
                impName = impName.substring(impName.lastIndexOf(".") + 1);
            }
            if (impName.equals(parts.get(0))) {
                TypeItem ret = new TypeItem(imp);
                for (String s : subtypes) {
                    if (!subtypes.isEmpty() && parts.size() > 1) {
                        continue;
                    }
                    UnresolvedAVM2Item su = new UnresolvedAVM2Item(new ArrayList<String>(), importedClasses, true, null, line, s, null, openedNamespaces);
                    su.resolve(paramTypes, paramNames, abc, otherAbcs, callStack, variables);
                    if (!(su.resolved instanceof TypeItem)) {
                        throw new CompilationException("Not a type", line);
                    }
                    TypeItem st = (TypeItem) su.resolved;
                    ret.subtypes.add(st.fullTypeName);
                }
                resolved = ret;
                for (int i = 1; i < parts.size(); i++) {
                    resolved = new PropertyAVM2Item(resolved, parts.get(i), null, abc, otherAbcs, openedNamespaces, new ArrayList<MethodBody>());
                    if (i == parts.size() - 1) {
                        ((PropertyAVM2Item) resolved).index = index;
                        ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                    }
                }
                if (parts.size() == 1 && index != null) {
                    throw new CompilationException("Types do not have indices", line);
                }
                if (parts.size() == 1 && assignedValue != null) {
                    throw new CompilationException("Cannot assign type", line);
                }
                return ret;
            }
        }

        //Search all fully qualitfied types
        List<ABC> allAbcs = new ArrayList<>();
        allAbcs.add(abc);
        allAbcs.addAll(otherAbcs);
        for (int i = 0; i < parts.size(); i++) {
            String fname = Helper.joinStrings(parts.subList(0, i + 1), ".");
            for (ABC a : allAbcs) {
                for (int c = 0; c < a.instance_info.size(); c++) {
                    if (fname.equals(a.instance_info.get(c).getName(a.constants).getNameWithNamespace(a.constants))) {
                        if (!subtypes.isEmpty() && parts.size() > i + 1) {
                            continue;
                        }
                        TypeItem ret = new TypeItem(fname);
                        for (String s : subtypes) {
                            UnresolvedAVM2Item su = new UnresolvedAVM2Item(new ArrayList<String>(), importedClasses, true, null, line, s, null, openedNamespaces);
                            su.resolve(paramTypes, paramNames, abc, otherAbcs, callStack, variables);
                            if (!(su.resolved instanceof TypeItem)) {
                                throw new CompilationException("Not a type", line);
                            }
                            TypeItem st = (TypeItem) su.resolved;
                            ret.subtypes.add(st.fullTypeName);
                        }
                        resolved = ret;
                        for (int j = i + 1; j < parts.size(); j++) {
                            resolved = new PropertyAVM2Item(resolved, parts.get(j), null, abc, otherAbcs, openedNamespaces, new ArrayList<MethodBody>());
                            if (j == parts.size() - 1) {
                                ((PropertyAVM2Item) resolved).index = index;
                                ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                            }
                        }
                        if (parts.size() == i + 1 && index != null) {
                            throw new CompilationException("Types do not have indices", line);
                        }
                        if (parts.size() == i + 1 && assignedValue != null) {
                            throw new CompilationException("Cannot assign type", line);
                        }

                        return ret;
                    }
                }
            }
        }
        
        //Search for types in opened namespaces        
        for (int ni : openedNamespaces) {
            Namespace ons = abc.constants.getNamespace(ni);            
            for (ABC a : allAbcs) {
                for (int c = 0; c < a.instance_info.size(); c++) {
                    if ((a == abc && a.instance_info.get(c).getName(a.constants).namespace_index == ni) || (ons.kind != Namespace.KIND_PRIVATE && a.instance_info.get(c).getName(a.constants).getNamespace(a.constants).hasName(ons.getName(abc.constants), a.constants))) {
                        String cname = a.instance_info.get(c).getName(a.constants).getName(a.constants, new ArrayList<String>());
                        if (parts.get(0).equals(cname)) {
                            if (!subtypes.isEmpty() && parts.size() > 1) {
                                continue;
                            }
                            TypeItem ret = new TypeItem(a.instance_info.get(c).getName(a.constants).getNameWithNamespace(a.constants));
                            for (String s : subtypes) {
                                UnresolvedAVM2Item su = new UnresolvedAVM2Item(new ArrayList<String>(), importedClasses, true, null, line, s, null, openedNamespaces);
                                su.resolve(paramTypes, paramNames, abc, otherAbcs, callStack, variables);
                                if (!(su.resolved instanceof TypeItem)) {
                                    throw new CompilationException("Not a type", line);
                                }
                                TypeItem st = (TypeItem) su.resolved;
                                ret.subtypes.add(st.fullTypeName);
                            }
                            resolved = ret;
                            for (int i = 1; i < parts.size(); i++) {
                                resolved = new PropertyAVM2Item(resolved, parts.get(i), null, abc, otherAbcs, openedNamespaces, new ArrayList<MethodBody>());
                                if (i == parts.size() - 1) {
                                    ((PropertyAVM2Item) resolved).index = index;
                                    ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                                }
                            }
                            if (parts.size() == 1 && index != null) {
                                throw new CompilationException("Types do not have indices", line);
                            }
                            if (parts.size() == 1 && assignedValue != null) {
                                throw new CompilationException("Cannot assign type", line);
                            }

                            return ret;
                        }
                    }
                }
            }
        }

        if (paramNames.contains(parts.get(0)) || parts.get(0).equals("arguments")) {
            int ind = paramNames.indexOf(parts.get(0));

            GraphTargetItem ret = new NameAVM2Item(ind == -1 ? TypeItem.UNBOUNDED : paramTypes.get(ind), line, name, null, false, openedNamespaces);
            resolved = ret;
            for (int i = 1; i < parts.size(); i++) {
                resolved = new PropertyAVM2Item(resolved, parts.get(i), null, abc, otherAbcs, openedNamespaces, new ArrayList<MethodBody>());
                if (i == parts.size() - 1) {
                    ((PropertyAVM2Item) resolved).index = index;
                    ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                }
            }
            if (parts.size() == 1) {
                ((NameAVM2Item) ret).setIndex(index);
                ((NameAVM2Item) ret).setAssignedValue(assignedValue);
            }
            return ret;
        }

        if(!subtypes.isEmpty() && parts.size()==1 && parts.get(0).equals("Vector")){
            TypeItem ret = new TypeItem("__AS3__.vec.Vector");
            for (String s : subtypes) {
                UnresolvedAVM2Item su = new UnresolvedAVM2Item(new ArrayList<String>(), importedClasses, true, null, line, s, null, openedNamespaces);
                su.resolve(paramTypes, paramNames, abc, otherAbcs, callStack, variables);
                if (!(su.resolved instanceof TypeItem)) {
                    throw new CompilationException("Not a type", line);
                }
                TypeItem st = (TypeItem) su.resolved;
                ret.subtypes.add(st.fullTypeName);
            }
            resolved = ret;
            return ret;
        }
        
        if(mustBeType){
            throw new CompilationException("Not a type", line);
        }
        resolved = null;
        GraphTargetItem ret = null;
        for (int i = 0; i < parts.size(); i++) {
            resolved = new PropertyAVM2Item(resolved, parts.get(i), (i == parts.size() - 1) ? index : null, abc, otherAbcs, openedNamespaces, callStack);
            if (ret == null) {
                ((PropertyAVM2Item) resolved).scopeStack = scopeStack;
                ret = resolved;
            }
        }
        return ret;
    }

}