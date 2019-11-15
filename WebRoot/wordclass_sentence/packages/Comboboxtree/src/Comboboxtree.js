
Ext.define("Ext.ux.Comboboxtree", {
    extend: "Ext.form.field.Picker",
	alias: "widget.comboboxtree",
    requires: ["Ext.tree.Panel"],
    config: {
    	submitValue: ''
    },
    initComponent: function() {
        var self = this;
        Ext.apply(self, {
            fieldLabel: self.fieldLabel,
            labelWidth: self.labelWidth
        });
        self.callParent();
    },
    
    clearValue : function() {
        var me = this;
        var picker = me.getPicker();
        var root = picker.getRootNode();
        root.cascadeBy(function(node){
            if(node.get('checked')!= null){
                node.set('checked', false);
            }
        });
        me.setSubmitValue("");
        me.setValue("");
    },
    bindValue : function(value, submitValue) {
        var me = this;
        var picker = me.getPicker();
        var root = picker.getRootNode();
        var ids = submitValue.split(",");
 
        root.cascadeBy(function(node){
            if(node.get('checked')!= null){
                node.set('checked', false);
                for (var i = 0; i < ids.length; i++){
                    if(node.get('id') == ids[i]){
                        node.set('checked', true);
                    }
                }
            }
        });
 
        me.setSubmitValue(submitValue);
        me.setValue(value);
    },
    createPicker: function() {
        var self = this;
        var store = Ext.create('Ext.data.TreeStore', {
            proxy: {
                type: 'ajax',
                url: self.storeUrl,
                extraParams: {
                	sign: curwordid,
                	isitem:self.isitem
                }
            },
            sorters: [
            	
            /*{
            	property: 'order',
            	direction: 'ASC'	
            },
            	{
                property: 'id',
                direction: ''
            },
            {
                property: 'text',
                direction: ''
            },
            {
            	property: 'leaf',
            	direction: ''
            }*/]
//            root: {
//                id: self.rootId,
//                text: self.rootText
//            },
            //nodeParameter: self.treeNodeParameter
        });
        self.picker = new Ext.tree.Panel({
            height: 300,
            autoScroll: true,
            floating: true,
            focusOnToFront: false,
            shadow: true,
            ownerCt: this.ownerCt,
            useArrows: true,
            store: store,
            rootVisible: false
        });
        self.picker.on({
            checkchange: function(record, checked) {
            	Array.prototype.indexOf = function(val) { 
					for (var i = 0; i < this.length; i++) { 
						if (this[i] == val) return i; 
					} 
					return -1; 
				};
				Array.prototype.remove = function(val) { 
					var index = this.indexOf(val); 
					if (index > -1) { 
						this.splice(index, 1); 
					} 
				};
                var checkModel = self.checkModel;
                if (checkModel == 'double') {
                    var root = self.picker.getRootNode();
                    root.cascadeBy(function(node) {
                        if (node.get('text') != record.get('text')) {
                            node.set('checked', false);
                        }
                    });
                    if (record.get('leaf') && checked) {
 
                        self.setSubmitValue(record.get('id')); // 隐藏值
                        self.setValue(record.get('text')); // 显示值
                    } else {
                        record.set('checked', false);
                        self.setSubmitValue(''); // 隐藏值
                        self.setValue(''); // 显示值
                    }
                } else {
 
                    var cascade = self.cascade;
 
                    if (checked == true) {
                        if (cascade == 'both' || cascade == 'child' || cascade == 'parent') {
                            if (cascade == 'child' || cascade == 'both') {
                                if (!record.get("leaf") && checked) record.cascadeBy(function(record) {
                                    record.set('checked', true);
                                });
 
                            }
                            if (cascade == 'parent' || cascade == 'both') {
                                pNode = record.parentNode;
                                for (; pNode != null; pNode = pNode.parentNode) {
                                    pNode.set("checked", true);
                                }
                            }
 
                        }
                        names = [],
		                values = [];
                        if (record.get("text")=="全选"){
                        	var records = this.getRootNode().childNodes;
	                    	Ext.Array.each(records,
	                    	function(rec) {
	                    		if(rec.get("text")=="全国"){
		                        	rec.set('checked', false);
	                    		}else{
	                    			names.push(rec.get('text'));
		                        	values.push(rec.get('id'));
	                    			rec.set('checked', true);
	                    			var childs = rec.childNodes;
	                    			if(childs.length>0){
	                    				Ext.Array.each(childs,
	                    				function(rec1) {
	                    					names.push(rec1.get('text'));
		                        			values.push(rec1.get('id'));
	                    					rec1.set('checked', true);
	                    				});
	                    			}
	                    		}
	                    	});
	                    	names.remove("全选");
	                    	values.remove("全选");
		                    self.setSubmitValue(values.join(",")); // 隐藏值
	                    	self.setValue(names.join(",")); // 显示值
                        }else {
                        
		                    var records = self.picker.getView().getChecked();                    
		                    Ext.Array.each(records,
		                    function(rec) {
		                        names.push(rec.get('text'));
		                        values.push(rec.get('id'));
		                    });
		                    if(record.get("text")=="全国"){
		                    	Ext.Array.each(records,
		                    	function(rec) {
		                    		if(rec.get("text")!="全国"){
			                        	rec.set('checked', false);
		                    		}
		                    	});
		                    	self.setSubmitValue("全国"); // 隐藏值
		                    	self.setValue("全国"); // 显示值
		                    }else{
		                    	
								Ext.Array.each(records,
								function(rec) {
		                    		if(rec.get("text")=="全国"){
			                        	rec.set('checked', false);
		                    		}
		                    	});
		                    	values.remove("全国");
		                    	names.remove("全国");
		                    	
			                    self.setSubmitValue(values.join(',')); // 隐藏值
			                    self.setValue(names.join(',')); // 显示值
		                    }
                        }
                    } else if (checked == false) {
                        if (cascade == 'both' || cascade == 'child' || cascade == 'parent') {
                            if (cascade == 'child' || cascade == 'both') {
                                if (!record.get("leaf") && !checked) record.cascadeBy(function(record) {
 
                                    record.set('checked', false);
 
                                });
                            }
 
                        }
	                    names = [];
	                    values = [];
                        if(record.get("text")=="全选"){
                        	var records = this.getRootNode().childNodes;
                        	Ext.Array.each(records,
		                    function(rec) {
		                        rec.set("checked",false);
		                        var rec1 = rec.childNodes;
		                        if(rec1.length>0){
		                        	Ext.Array.each(rec1,
		                        		function(rec2){
		                        			rec2.set("checked",false);
		                        		}
		                        	);
		                        }
		                    });
		                    self.setSubmitValue(values); // 隐藏值
		                    self.setValue(names); // 显示值
                        }else{
	                        var records = self.picker.getView().getChecked();
		                    Ext.Array.each(records,
		                    function(rec) {
		                        names.push(rec.get('text'));
		                        values.push(rec.get('id'));
		                    });
		                    self.setSubmitValue(values.join(',')); // 隐藏值
		                    self.setValue(names.join(',')); // 显示值
	          	         }
                    }
                }
            },
            itemclick: function(tree, record, item, index, e, options) {
                var checkModel = self.checkModel;
                if (checkModel == 'single') {
                    if (record.get('leaf')) {
                        self.setSubmitValue(record.get('id')); // 隐藏值
                        self.setValue(record.get('text')); // 显示值
                    } else {
                        self.setSubmitValue(''); // 隐藏值
                        self.setValue(''); // 显示值
                    }
                }
 
            }
        });
        return self.picker;
    },
    alignPicker: function() {
        var me = this,
        picker, isAbove, aboveSfx = '-above';
        if (this.isExpanded) {
            picker = me.getPicker();
            if (me.matchFieldWidth) {
                picker.setWidth(me.bodyEl.getWidth());
            }
            if (picker.isFloating()) {
                picker.alignTo(me.inputEl, "", me.pickerOffset); // ""->tl
                isAbove = picker.el.getY() < me.inputEl.getY();
                me.bodyEl[isAbove ? 'addCls': 'removeCls'](me.openCls + aboveSfx);
                picker.el[isAbove ? 'addCls': 'removeCls'](picker.baseCls + aboveSfx);
            }
        }
    }
});
