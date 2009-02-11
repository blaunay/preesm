package org.ietr.preesm.plugin.codegen.model.idl;

import java.util.Enumeration;
import java.util.HashMap;

import org.ietr.preesm.plugin.codegen.model.CodeGenArgument;
import org.ietr.preesm.plugin.codegen.model.CodeGenParameter;
import org.ietr.preesm.plugin.codegen.model.FunctionCall;
import org.ietr.preesm.plugin.codegen.model.IFunctionFactory;
import org.ietr.preesm.plugin.codegen.model.cal.CALFunctionFactory;
import org.jacorb.idl.AliasTypeSpec;
import org.jacorb.idl.ConstrTypeSpec;
import org.jacorb.idl.Declaration;
import org.jacorb.idl.Definition;
import org.jacorb.idl.Definitions;
import org.jacorb.idl.EnumType;
import org.jacorb.idl.IDLTreeVisitor;
import org.jacorb.idl.IdlSymbol;
import org.jacorb.idl.Interface;
import org.jacorb.idl.InterfaceBody;
import org.jacorb.idl.Method;
import org.jacorb.idl.Module;
import org.jacorb.idl.NativeType;
import org.jacorb.idl.OpDecl;
import org.jacorb.idl.Operation;
import org.jacorb.idl.ParamDecl;
import org.jacorb.idl.SimpleTypeSpec;
import org.jacorb.idl.Spec;
import org.jacorb.idl.StructType;
import org.jacorb.idl.TypeDeclaration;
import org.jacorb.idl.TypeDef;
import org.jacorb.idl.UnionType;
import org.jacorb.idl.Value;
import org.jacorb.idl.VectorType;
import org.jacorb.idl.parser;

public class IDLFunctionFactory implements IFunctionFactory, IDLTreeVisitor{

	public HashMap<String, FunctionCall> createdIdl;
	private FunctionCall currentCall ;
	
	public static IDLFunctionFactory instance = null;
	
	public static IDLFunctionFactory getInstance(){
		if(instance == null){
			instance = new IDLFunctionFactory() ;
		}
		return instance ;
	}
	
	
	private IDLFunctionFactory() {
		createdIdl = new HashMap<String, FunctionCall>();
	}
	
	@Override
	public FunctionCall create(String idlPath) {
		if(createdIdl.get(idlPath) == null){
			currentCall = null ;
			parser.setGenerator(this);
			try {
				IDLParser.parse(idlPath, this);
				createdIdl.put(idlPath, currentCall);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			currentCall = null ;
		}
		return createdIdl.get(idlPath);
	}

	@Override
	public void visitAlias(AliasTypeSpec arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitConstrTypeSpec(ConstrTypeSpec arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitDeclaration(Declaration arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitDefinition(Definition arg0) {
		System.out.println(arg0.toString());
		arg0.get_declaration().accept(this);
	}

	@Override
	public void visitDefinitions(Definitions arg0) {
		System.out.println(arg0.toString());
        Enumeration e = arg0.getElements();
        while( e.hasMoreElements() )
        {
            IdlSymbol s = (IdlSymbol)e.nextElement();
            s.accept( this );
        }
		
	}

	@Override
	public void visitEnum(EnumType arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitInterface(Interface arg0) {
		currentCall = new FunctionCall(arg0.name());
		arg0.body.accept(this);
		System.out.println(arg0.toString());
	}

	@Override
	public void visitInterfaceBody(InterfaceBody arg0) {
		Operation[] ops = arg0.getMethods();
        for( int i = 0; i < ops.length; i++ )
        {
            ops[ i ].accept( this );
        }
        System.out.println(arg0.toString());
	}

	@Override
	public void visitMethod(Method arg0) {
		if(arg0.name().equals("loop")){
			arg0.parameterType.accept(this);
		}
		System.out.println(arg0.toString());
	}

	@Override
	public void visitModule(Module arg0) {
		arg0.getDefinitions().accept(this);
		System.out.println(arg0.toString());
	}

	@Override
	public void visitNative(NativeType arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitOpDecl(OpDecl arg0) {
		if(arg0.name().equals("loop")){
			for(Object param : arg0.paramDecls){
				((ParamDecl) param).accept(this);
			}
		}
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitParamDecl(ParamDecl arg0) {
        if( arg0.paramAttribute == ParamDecl.MODE_IN ){
        	if(arg0.paramTypeSpec.name().equals("parameter")){
        		CodeGenParameter parameter = new CodeGenParameter(arg0.simple_declarator.name());
        		currentCall.addParameter(parameter);
        	}else{
        		CodeGenArgument argument = new CodeGenArgument(arg0.simple_declarator.name());
        		currentCall.addInput(argument);
        	}
        }else if( arg0.paramAttribute == ParamDecl.MODE_OUT ){
        	CodeGenArgument argument = new CodeGenArgument(arg0.simple_declarator.name());
        	currentCall.addOutput(argument);
        }
        System.out.println(arg0.toString());
	}

	@Override
	public void visitSimpleTypeSpec(SimpleTypeSpec arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitSpec(Spec arg0) {
        Enumeration e = arg0.definitions.elements();
        while( e.hasMoreElements() )
        {
            IdlSymbol s = (IdlSymbol)e.nextElement();
            s.accept( this );
        }
        System.out.println(arg0.toString());
	}

	@Override
	public void visitStruct(StructType arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitTypeDeclaration(TypeDeclaration arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitTypeDef(TypeDef arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visitUnion(UnionType arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitValue(Value arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}

	@Override
	public void visitVectorType(VectorType arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.toString());
	}
	
	public static void main(String [] args){
		if (args.length != 1) {
            return;
        }
		IDLFunctionFactory factory = new IDLFunctionFactory();
		FunctionCall call = factory.create(args[0]);
		System.out.println("creation done");
	}

}
