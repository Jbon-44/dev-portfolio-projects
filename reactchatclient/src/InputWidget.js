import React from 'react';


function InputWidget({ label, placeholder, value, setValue }) {
  return (
    <div>
      <label>{label}: </label>
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
    </div>
  );
}

export default InputWidget;