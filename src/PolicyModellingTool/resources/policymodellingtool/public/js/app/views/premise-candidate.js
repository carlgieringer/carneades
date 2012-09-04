PM.PremiseCandidateView = Backbone.View.extend(
    {className: "premise-candidate",
     
     events: {
         "change .role-input": "role_changed",
         "change input[type=hidden]": "statement_changed",
         "click .delete": "on_delete_premise",
         "click .create": "create_statement"
     },
     
     initialize: function() {
         this.model.on('change', this.render, this);
         _.bindAll(this, 'role_changed', 'statement_changed', 
                   'render', 'on_delete_premise', 'create_statement');
     },
     
     render: function() {
         var data = this.model.toJSON();
         
         this.$el.html(ich.premisecandidate());
         
         var role = this.$('.role-input');
         role.prop('disabled', !data.editableRole);
         if(data.premise.role) {
             role.val(data.premise.role);    
         } 
         
         var statement = this.statement();
         statement.select2({data: {results: data.statements.toJSON(),
                                   text: function(statement) {
                                       return AGB.statement_text(statement);
                                   }
                                  },
                            placeholder: "Select a statement",
                            formatSelection: AGB.format_selected_statement,
                            formatResult: AGB.statement_text,
                            initSelection: function(element, callback) {
                                var statement = data.statements.get(element.val());
                                if(statement) {
                                    callback(statement.toJSON()); 
                                } 
                            }});

         if(data.premise.statement) {
             statement.val(data.premise.statement.id).trigger('change');    
         } 
         
         return this;
     },
     
     statement: function() {
         return this.$('input[type=hidden]');
     },
     
     role_changed: function() {
         var role = this.$('.role-input').val();
         var premise = _.clone(this.model.get('premise'));
         premise.role = role;
         this.model.set('premise', premise);
     },
     
     statement_changed: function() {
         var statement = this.model.get('statements').get(this.statement().val());
         
         if(!_.isNil(statement)) {
             var premise = _.clone(this.model.get('premise'));
             premise.statement = statement.attributes;
             this.model.set('premise', premise);
         } 
     },
     
     on_delete_premise: function() {
         // removes the PremiseCandidate from the PremisesCandidates collection
         this.model.get('container').remove(this.model);
         
         this.statement().val(undefined).trigger('change');
         
         // removes the view
         this.remove();
         return false;
     },
     
     create_statement: function() {
         // TODO if a scheme is selected, prefills the atom
         var self = this;
         AGB.show_statement_editor({atom: "",
                                   save_callback: function(data) {
                                       var id = data.id;
                                       var statements = self.model.get('statements');
                                       var statement = statements.get(id);
                                       
                                       var premise = _.clone(self.model.get('premise'));
                                       premise.statement = statement; 
                                       self.model.set('premise', premise);
                                   }
                                   });
     }
     
    }
);

