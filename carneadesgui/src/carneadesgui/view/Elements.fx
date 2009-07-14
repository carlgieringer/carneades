/*
Carneades Argumentation Library and Tools.
Copyright (C) 2008 Matthias Grabmair

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License version 3 (GPL-3)
as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package carneadesgui.view;

import javafx.scene.paint.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.*;
import javafx.scene.*;


// import the necessary parts of the model
import carneadesgui.model.Argument;
import carneadesgui.model.Argument.*;

// Other View Imports
import carneadesgui.view.*;
import carneadesgui.GC.*;

// control import
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Transform;
import javafx.util.Math;

import javafx.scene.shape.Shape;

import javafx.scene.transform.Translate;
import javafx.scene.transform.Rotate;

/**
* Auxiliary class for centered text.
*/
public class CenteredStatementText extends Text {

	override var transforms = bind [
		Translate {
			x: - boundsInLocal.width / 2
			y: - boundsInLocal.height / 2
		}
	];

	public function setText(s: String): Void {
		content = s;
	}

	override var textAlignment = TextAlignment.CENTER;
	override var textOrigin = TextOrigin.TOP;
}

/**
 * Base class for model-specific graph vertices.
 */
public abstract class ArgumentElement extends Vertex {

	/**
	 * The filler color.
	 */
	var fill: Color = Color.WHITE;

	var selection: Shape = Rectangle {
					blocksMouse: false
					fill: Color.TRANSPARENT
					x: bind x - (width / 2) - 5
					y: bind y - (height / 2) - 5
					width: bind width + 10
					height: bind height + 10
					stroke: bind {if (control.dragging) dragColor else selectionColor};
					strokeWidth: 2
					visible: bind selected
				} // selection rect

	 var middlePoint: Circle = Circle {
		centerX: bind x
		centerY: bind y
		radius: 3
		fill: Color.RED
		visible: bind drawDebug
	}
}

/**
 * The argument view object. Currently it is a circle, but it used to be a box, hence the name.
 */
public class ArgumentBox extends ArgumentElement {

	/**
	 * The represented model argument object.
	 */
	public var argument: Argument;

	// bind the model element to the argument element
	override var model = bind argument;

	override var height = argumentCircleDefaultRadius * 2;
	override var defaultWidth = argumentCircleDefaultRadius * 2;
	override var scaleWithText = false;
	override var caption = bind argument.id;
	override var fill = bind {if (argument.ok) Color.LIGHTGREY else Color.WHITE};
	override var bottomBrink = argumentBoxBottomBrink;

	override var text = CenteredStatementText {
					content: bind {
						if ((argument.conclusion.standard) instanceof BestArgument
							or (argument.conclusion.standard) instanceof Preponderance
							or (argument.conclusion.standard) instanceof ClearAndConvincingEvidence
							or (argument.conclusion.standard) instanceof BeyondReasonableDoubt
							)
							"{if (argument.pro) '+' else '-'}.{(argument.weight * 100) as Integer}"
							else "{if (argument.pro) '+' else '-'}"}
					textAlignment: TextAlignment.CENTER
					textOrigin: TextOrigin.BASELINE
					x: bind x
					// The text Y coordinate positioning is dirty as the text currently lacks a currentheight attribute
					y: bind y + 10
				} // Text

	 var mainCircle: Circle = Circle {
		centerX: bind x
		centerY: bind y
		radius: bind argumentCircleDefaultRadius

		fill: bind {
			if (not argument.ok) Color.WHITE
			else {
				if (not argument.pro) argumentConColor else argumentProColor
			}
		};

		stroke: Color.BLACK
		blocksMouse: false

		effect: {
			if (drawShadows) {
				DropShadow {
					color: bind shadowColor
					offsetX: bind xShadowShift
					offsetY: bind yShadowShift
					radius: bind shadowBlurRadius
				}
			} else null
		}

		onMouseClicked: function(e: MouseEvent): Void {
			control.processGraphSelection(this);
		}

		onMouseDragged: function(e: MouseEvent) {
			if (this.selected) { control.startDrag(); }
		}

		onMouseReleased: function(e: MouseEvent) {
			if (control.dragging) {	control.endDrag(); }
		}

		onMouseEntered: function(e: MouseEvent) {
			if (control.dragging) { control.setDraggingOver(this); }
		}

		onMouseExited: function(e: MouseEvent) {
			if (control.dragging) { control.setDraggingOver(null); }
		}
	}

	override var selection = Circle {
		fill: Color.TRANSPARENT
		centerX: bind x
		centerY: bind y
		radius: argumentCircleDefaultRadius + 5
		stroke: bind {if (control.dragging) dragColor else selectionColor};
		strokeWidth: 2
		visible: bind selected
	} // selection circle


	override function create():Node {
		Group {
			content: [
				mainCircle,
				selection,
				text,
				middlePoint
			] // content
		} // Group
	} // composeNode
}

/**
 * The statement view object.
 */
public class StatementBox extends ArgumentElement {

	/**
	 * The represented model statement.
	 */
	public var statement: Statement;

	// bind the model element to the statement element
	override var model = bind statement;

	override var scaleWithText = false;
	override var defaultWidth = statementBoxDefaultWidth;
	override var bottomBrink = statementBoxBottomBrink;
	override var caption = bind { if (statement.wff.length() < numDisplayedChars) statement.wff
										else "{statement.wff.substring(0, numDisplayedChars-1)}..."};
	var status: String = bind statement.getBoundStatus();
	var statusColor = bind {
		if (status == "stated") statusStatedColor
		else if (status == "assumed true") statusAssumedTrueColor
		else if (status == "assumed false") statusAssumedFalseColor
		else if (status == "rejected") statusRejectedColor
		else if (status == "accepted") statusAcceptedColor
		else /*if (status == "questioned")*/ statusQuestionedColor
	};

	override var text = CenteredStatementText {
					blocksMouse: false
					content: bind caption
					x: bind x 
					y: bind y 
					wrappingWidth: bind width //- 2*boxTextPadding
				} // Text

	var mainRect: Polygon = Polygon {
		points: bind [
			x - (width / 2), y - (height / 2),
			x - (width / 2) + width, y - (height / 2),
			x - (width / 2) + width, y - (height / 2) + height,
			x - (width / 2), y - (height / 2) + height ]
		blocksMouse: true

		//x: bind x - (width / 2)
		//y: bind y - (height / 2)
		//width: bind width
		//height: bind height
		fill: bind { if (fillStatements) statusColor else defaultBoxFill }
		stroke: bind { if (fillStatements) Color.BLACK else statusColor }
		strokeWidth: 1

		effect: {
			if (drawShadows) {
				DropShadow {
					color: bind shadowColor
					offsetX: bind xShadowShift
					offsetY: bind yShadowShift
					radius: bind shadowBlurRadius
				}
			} else null
		}

		onMouseClicked: function(e: MouseEvent): Void {
			control.processGraphSelection(this);

			if (debug) {
				print();
			}
		}
					
		onMouseDragged: function(e: MouseEvent) {
			if (this.selected) { control.startDrag(); }
		}

		onMouseReleased: function(e: MouseEvent) {
			if (control.dragging) {	control.endDrag(); }
		}

		onMouseEntered: function(e: MouseEvent) {
			if (control.dragging) { control.setDraggingOver(this); }
		}

		onMouseExited: function(e: MouseEvent) {
			if (control.dragging) { control.setDraggingOver(null); }
		}
	} // main rect

	 var acceptableCircle: Circle = Circle {
		centerX: bind x + (this.width / 2) + acceptableCirclePadding + (acceptableCircleWidth / 2)
		centerY: bind y - (acceptableCirclePadding / 2) - (acceptableCircleWidth / 2)
		radius: bind (acceptableCircleWidth / 2)
		fill: bind { if (statement.ok) statusAcceptedColor else null }
		strokeWidth: 1
		stroke: Color.BLACK

		effect: {
			if (drawShadows) {
				DropShadow {
					color: bind shadowColor
					offsetX: bind xShadowShift
					offsetY: bind yShadowShift
					radius: bind shadowBlurRadius
				}
			} else null
		}
	}

	 var acceptableCompCircle: Circle = Circle {
		centerX: bind x + (this.width / 2) + acceptableCirclePadding + (acceptableCircleWidth / 2)
		centerY: bind y + (acceptableCirclePadding / 2) + (acceptableCircleWidth / 2)
		radius: bind (acceptableCircleWidth / 2)
		fill: bind { if (statement.complementOk) statusRejectedColor else null }
		strokeWidth: 1
		stroke: Color.BLACK
		effect: {
				if (drawShadows) {
					DropShadow {
					color: bind shadowColor
					offsetX: bind xShadowShift
					offsetY: bind yShadowShift
					radius: bind shadowBlurRadius
				}
			} else null
		}
	}

	override function create():Node {
		Group {
			content: [
				mainRect,
			    selection,
				text,
				acceptableCircle,
				acceptableCompCircle,
				middlePoint,
			] // content
		} // Group
	} // composeNode
}

/**
 * Class for edges that have arrow heads.
 */
public class Arrow extends Edge {

	/**
	 * The size of the arrowhead in side length pixels.
	 */
	public var headSize: Number = 10;

	/**
	 * 0 means it points right, 1 it points left, straight up or down does not matter
	 */
	public var heading: Number = 0;

	/**
	 * The color to fill the arrowhead with.
	 */
	public var fill: Color = Color.BLACK;

	override var stroke = Color.BLACK;
	override var turnHead = true;

	postinit {
		setAngle();
	}

	override function create():Node {
		Group {
			content: [
				line,
				Polygon {
					transforms: bind [Transform.rotate(angle, x2, y2)]

					points: bind [ x2, y2,
								x2 - headSize, y2+(headSize / 2),
								x2 - headSize, y2-(headSize / 2)]
					stroke: bind stroke
					strokeWidth: bind strokeWidth
					fill: bind fill
				}
			] // content
		} // group
	}
}

/**
 * Argument link specific arrow class to set base attributes.
 */
public class ArgumentLink extends Arrow {
	override var headSize = 10;
	override var yHeadShift = bind headSize / 3;

	public var argument: Argument = null;
	// bind the model element to the argument element
	override var model = bind argument;

	override var fill = bind {if (argument.pro) Color.BLACK else Color.WHITE};
}

/**
 * Class for premise link edges.
 */
public class PremiseLink extends Edge {

	/**
	 * The represented model premise object.
	 */
	public var premise: Premise;

	// bind the model element to the premise element
	override var model = bind premise;

	/**
	 * The radius of the head at the edges end. Deprecated! Was used in older design of assumptions and exceptions.
	 */
	public var radius: Number = 5;

	/**
	 * The width of the negation bar.
	 */
	public var negWidth: Number = 10;

	/**
	 * The stroke width of the negation bar.
	 */
	var negStrokeWidth: Number = 2;

	override var dashed = bind premise.exception;

	/**
	 * Shall the negation bar be drawn?
	 */
	public var negated: Boolean = false;

	override var turnHead = bind negated;

	var negation: Line = Line {
					startX: bind x2 - Math.max(yHeadShift * 2, negWidth)
					startY: bind y2 - (negWidth / 2)
					endX: bind x2 - Math.max(yHeadShift * 2, negWidth)
					endY: bind y2 + (negWidth / 2)
					transforms: bind [
						Rotate {
							angle: bind angle
							pivotX: bind x2
							pivotY: bind y2
						}
					]
					stroke: bind stroke
					strokeWidth: bind negStrokeWidth
					visible : bind negated
				}

	// exceptionhead currently not in content because of dashed lines
	var exceptionHead: Circle = Circle {
   				     centerX: bind x2
   				     centerY: bind y2
   	    			 radius: bind radius
   				     fill: Color.WHITE
   				     stroke: Color.BLACK
					 visible: bind premise.exception
    			}

	/**
	 * Switches the negation bar on and off. Used to update the view more efficiently as the tree layout does not need to be updated.
	 */
	public function negate() {
		if (negated) {
			negated = false;
		} else {
			negated = true;
		}
	}

	override function create():Node {
		Group {
			content: [
				line, negation, exceptionHead
			] // content
		} // Group
	} // composeNode
}

