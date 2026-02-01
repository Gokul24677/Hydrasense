from flask import Flask, request, jsonify
import sqlite3
from datetime import datetime
import os
from pyngrok import ngrok
from werkzeug.security import generate_password_hash, check_password_hash
import subprocess
import re

app = Flask(__name__)
DB_PATH = 'hydrasense.db'

def get_db_connection():
    try:
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row
        return conn
    except sqlite3.Error as e:
        print(f"Error connecting to database: {e}")
        return None

def init_db():
    conn = get_db_connection()
    if conn:
        cursor = conn.cursor()
        # Schema: User ID, Password, pH Levels, Timestamp, Date
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                password TEXT NOT NULL,
                ph_level REAL NOT NULL,
                timestamp TEXT NOT NULL,
                date TEXT NOT NULL
            )
        ''')
        conn.commit()
        
        # Add seed user abcd/1111 if not exists
        cursor.execute('SELECT 1 FROM readings WHERE user_id = ?', ('abcd',))
        if not cursor.fetchone():
            from werkzeug.security import generate_password_hash
            hashed = generate_password_hash('1111')
            now = datetime.now()
            cursor.execute('''
                INSERT INTO readings (user_id, password, ph_level, timestamp, date)
                VALUES (?, ?, ?, ?, ?)
            ''', ('abcd', hashed, 7.0, now.strftime("%H:%M:%S"), now.strftime("%Y-%m-%d")))
            conn.commit()
            
        conn.close()

@app.route('/')
def home():
    return "HydraSense Backend is Running!", 200

@app.route('/register', methods=['POST'])
def register():
    # Reuse add_reading logic for registration in this simple schema
    return add_reading()

@app.route('/add_reading', methods=['POST'])
def add_reading():
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    user_id = data.get('user_id')
    password = data.get('password')
    ph_level = data.get('ph_level', 0.0)
    
    if not all([user_id, password]):
        return jsonify({"error": "Missing user_id or password"}), 400
    
    hashed_password = generate_password_hash(password)
    now = datetime.now()
    timestamp = now.strftime("%H:%M:%S")
    date = now.strftime("%Y-%m-%d")
    
    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "Database error"}), 500
        
    try:
        cursor = conn.cursor()
        cursor.execute('SELECT password FROM readings WHERE user_id = ?', (user_id,))
        user = cursor.fetchone()
        
        if user:
            cursor.execute('''
                INSERT INTO readings (user_id, password, ph_level, timestamp, date)
                VALUES (?, ?, ?, ?, ?)
            ''', (user_id, user['password'], ph_level, timestamp, date))
        else:
            cursor.execute('''
                INSERT INTO readings (user_id, password, ph_level, timestamp, date)
                VALUES (?, ?, ?, ?, ?)
            ''', (user_id, hashed_password, ph_level, timestamp, date))
            
        conn.commit()
        return jsonify({"message": "Reading added successfully"}), 201
    except sqlite3.Error as e:
        return jsonify({"error": str(e)}), 500
    finally:
        conn.close()

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400
        
    user_id = data.get('user_id')
    password = data.get('password')
    
    if not all([user_id, password]):
        return jsonify({"error": "Missing credentials"}), 400
        
    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "Database error"}), 500
        
    try:
        cursor = conn.cursor()
        cursor.execute('SELECT password FROM readings WHERE user_id = ? LIMIT 1', (user_id,))
        row = cursor.fetchone()
        
        if row and check_password_hash(row['password'], password):
            return jsonify({"message": "Login successful"}), 200
        else:
            return jsonify({"error": "Invalid email or password"}), 401
    except sqlite3.Error as e:
        return jsonify({"error": str(e)}), 500
    finally:
        conn.close()

@app.route('/get_readings', methods=['GET'])
def get_readings():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({"error": "User ID required"}), 400
        
    conn = get_db_connection()
    if not conn:
        return jsonify({"error": "Database error"}), 500
        
    try:
        cursor = conn.cursor()
        cursor.execute('SELECT user_id, ph_level, timestamp, date FROM readings WHERE user_id = ?', (user_id,))
        rows = cursor.fetchall()
        
        readings = [dict(row) for row in rows]
        return jsonify(readings), 200
    except sqlite3.Error as e:
        return jsonify({"error": str(e)}), 500
    finally:
        conn.close()

@app.route('/discover_devices', methods=['GET'])
def discover_devices():
    try:
        # Run 'arp -a' to find devices on the local network
        result = subprocess.check_output(['arp', '-a'], stderr=subprocess.STDOUT, shell=True).decode('utf-8')
        
        devices = []
        # Regex to find IP and MAC addresses in arp -a output
        # Example line: 192.168.1.1           00-11-22-33-44-55     dynamic
        pattern = r"(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\s+([0-9a-f-]{17})"
        
        for line in result.splitlines():
            match = re.search(pattern, line, re.IGNORECASE)
            if match:
                devices.append({
                    "ip": match.group(1),
                    "mac": match.group(2)
                })
        
        return jsonify({
            "status": "success",
            "device_count": len(devices),
            "devices": devices
        }), 200
        
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


if __name__ == '__main__':
    init_db()
    
    # Open a ngrok tunnel to the HTTP server
    public_url = ngrok.connect(5000).public_url
    print(f" * ngrok tunnel available at: {public_url}")
    
    app.run(debug=False, port=5000)
