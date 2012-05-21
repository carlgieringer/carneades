// adds a format method to all string
// place holders are of the form {0}, {1} etc
String.prototype.format = function() {
  var args = arguments;
  return this.replace(/{(\d+)}/g, function(match, number) {
    return typeof args[number] != 'undefined'
      ? args[number]
      : match
    ;
  });
};

// replaces the object properties names with minus (-) to properties names with underscore
Object.prototype.normalize = function() {
    for(name in this) {
        if(typeof this[name] !== 'function') {
            var normalized = name.replace(/-/g, '_');
            if(normalized !== name) {
                this[normalized] = this[name];
                delete this[name];                            
            }
        }
    }
};

AGB.escape_html = function(text)
{
    return $('<div/>').text(text).html();
};

AGB.is_url = function(url)
{
    return /^(http|https|ftp)/.test(url);
};
