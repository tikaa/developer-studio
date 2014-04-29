package dataMapper.diagram.custom.configuration.operators;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import dataMapper.Element;
import dataMapper.Operator;
import dataMapper.OperatorLeftConnector;
import dataMapper.OperatorRightConnector;
import dataMapper.SchemaDataType;
import dataMapper.TreeNode;
import dataMapper.diagram.custom.configuration.function.AssignmentStatement;

public class ContainsTransformer implements OperatorsTransformer{

	private static final String INDEX = "[i]";

	@Override
	public AssignmentStatement transform(Operator operator) {
		EObject containsInput = getInputObject(operator);

		if (containsInput instanceof Element) {
			Element inputElement = (Element) containsInput;
			return getSimpleOperatorMapping(operator);

		} else if (containsInput instanceof OperatorRightConnector) {
			// FIXME implementation of operator chaining
		}

		return null;
	}

	private Element getOutputElement(Operator operator) {
		return operator.getBasicContainer().getRightContainer().getRightConnectors().get(0).getOutNode().getOutgoingLink().get(0).getInNode().getElementParent();
	}

	@Override
	public TreeNode getOutputElementParent(Operator operator) {
		Element outputElement = getOutputElement(operator);
		Element inputElement = (Element) getInputObject(operator);
		TreeNode outputParent = outputElement.getFieldParent();

		if (inputElement.getFieldParent().getSchemaDataType().equals(SchemaDataType.ARRAY) && !(outputParent.getSchemaDataType().equals(SchemaDataType.ARRAY))) {
			while (outputParent.getFieldParent() != null && !(outputParent.getSchemaDataType().equals(SchemaDataType.ARRAY))) {
				outputParent = outputParent.getFieldParent();
			}
			
			//TODO if there isn't array type in output schema, may be we have to set immediate parent of output element as array 
			// var outputParent = new array()
		}

		return outputParent;
	}

	private EObject getInputObject(Operator operator) {
		EList<OperatorLeftConnector> leftConnectors = operator.getBasicContainer().getLeftContainer().getLeftConnectors();
		EObject inputObject = leftConnectors.get(0).getInNode().getIncomingLink().get(0).getOutNode().eContainer();
		return inputObject;
	}

	private Operator getInputOperator(EObject in) {
		return (Operator) in.eContainer().eContainer().eContainer();
	}


	private AssignmentStatement getSimpleOperatorMapping(Operator operator) {
		ArrayList<Element> inputElements = getInputElements(operator);
		Element outputElement = getOutputElement(operator);
		TreeNode outputParent = getOutputElementParent(operator);

		TreeNode inputElementsParent = getInputElementParent(operator);
//		String assign ="";
		String assign = getTreeHierarchy(outputElement.getFieldParent(), outputParent) + "." + outputElement.getName() + " = " + getTreeHierarchy(inputElements.get(0).getFieldParent(), inputElementsParent) + "."+inputElements.get(0).getName()+".contains( " +getTreeHierarchy(inputElements.get(1).getFieldParent(), inputElementsParent)+"."+inputElements.get(1).getName() + " );";
		
//		for(Element input : inputElements){
//			String assign = getTreeHierarchy(outputElement.getFieldParent(), outputParent) + "." + outputElement.getName() + ".equel( " + input.getFieldParent().getName() + "." + input.getName() + " );";
//			
//		}

		AssignmentStatement statement = new AssignmentStatement();
		statement.setStatement(assign);
		return statement;
	}

	private String getTreeHierarchy(TreeNode tree, TreeNode parent) {
		StringBuilder hierarchy = new StringBuilder();

		while (!(tree.equals(parent))) {
			hierarchy.insert(0, tree.getName());
			hierarchy.insert(0, ".");
			tree = tree.getFieldParent();
		}

		if (tree.getSchemaDataType().equals(SchemaDataType.ARRAY)) {
			hierarchy.insert(0, (tree.getName() + INDEX));
		} else {
			hierarchy.insert(0, tree.getName());
		}

		return hierarchy.toString();

	}

	@Override
	public TreeNode getInputElementParent(Operator operator){
		ArrayList<Element> inputElements = getInputElements(operator);
		TreeNode highestParent = null;
		for (Element element : inputElements){
			if (element != null) {
				if (highestParent != null) {
					if (highestParent.getLevel() >= element.getFieldParent().getLevel()) {
						highestParent = element.getFieldParent();
					}
				} else {
					highestParent = element.getFieldParent();
				}
			}
		}
		
		if(getOutputElement(operator).getFieldParent().getSchemaDataType().equals(SchemaDataType.ARRAY) && !(highestParent.getSchemaDataType().equals(SchemaDataType.ARRAY))){
			while(highestParent.getFieldParent() != null && !(highestParent.getSchemaDataType().equals(SchemaDataType.ARRAY))){
				highestParent = highestParent.getFieldParent();
			}
		}
		
		return highestParent;
		
	}

	/**
	 * list of input elements needs to create assignment statements 
	 * @param operator  operator object
	 * @return input elements of input tree which have mapped to operator
	 */
	private ArrayList<Element> getInputElements(Operator operator) {
		EList<OperatorLeftConnector> leftConnectors = operator.getBasicContainer().getLeftContainer().getLeftConnectors();
		ArrayList<Element> elementList = new ArrayList<Element>();
		for(OperatorLeftConnector connector : leftConnectors){
			if(connector.getInNode().getIncomingLink().size() != 0){
				elementList.add(connector.getInNode().getIncomingLink().get(0).getOutNode().getElementParent());
			}
		}
		return elementList;
	}

}