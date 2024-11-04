from flask import Flask, render_template, request, jsonify
import google.generativeai as genai
from pytube import Search

app = Flask(__name__)

# Configure Gemini AI
GEMINI_API_KEY = "AIzaSyD5ojNcnQk6ZNdrTqOCSbKONRG7el-MDnk"  # Replace with your actual API key
genai.configure(api_key=GEMINI_API_KEY)
model = genai.GenerativeModel('gemini-pro')

def generate_roadmap(field, goal):
    prompt = f"""Create a learning roadmap for {field} with the goal: {goal}
    
    Please format it with these exact sections:
    
    <div class='roadmap-container'>
        <section class='mb-6'>
            <h2 class='text-xl font-bold text-blue-600 mb-3'>Prerequisites</h2>
            [List prerequisites here]
        </section>

        <section class='mb-6'>
            <h2 class='text-xl font-bold text-blue-600 mb-3'>Core Concepts</h2>
            [List core concepts here]
        </section>

        <section class='mb-6'>
            <h2 class='text-xl font-bold text-blue-600 mb-3'>Learning Path Timeline</h2>
            [Provide timeline here]
        </section>

        <section class='mb-6'>
            <h2 class='text-xl font-bold text-blue-600 mb-3'>Project Suggestions</h2>
            [List projects here]
        </section>

        <section class='mb-6'>
            <h2 class='text-xl font-bold text-blue-600 mb-3'>Additional Resources</h2>
            [List resources here]
        </section>
    </div>
    """
    
    try:
        response = model.generate_content(prompt)
        print("Generated response:", response.text)  # Debug print
        return response.text
    except Exception as e:
        print(f"Error generating roadmap: {e}")
        return None

def search_youtube_videos(query, max_results=3):
    try:
        search = Search(f"{query} tutorial")
        videos = []
        for result in search.results[:max_results]:
            videos.append({
                "title": result.title,
                "author": result.author,
                "url": f"https://youtube.com/watch?v={result.video_id}"
            })
        return videos
    except Exception as e:
        print(f"Error searching YouTube videos: {e}")
        return []

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/generate_roadmap', methods=['POST'])
def generate_roadmap_route():
    try:
        data = request.get_json()
        field = data.get('field')
        goal = data.get('goal')
        
        if not field or not goal:
            return jsonify({"error": "Field and goal are required"}), 400
        
        roadmap_content = generate_roadmap(field, goal)
        if not roadmap_content:
            return jsonify({"error": "Failed to generate roadmap"}), 500
        
        videos = search_youtube_videos(f"{field} {goal}")
        
        return jsonify({
            "roadmap": roadmap_content,
            "videos": videos
        })
    except Exception as e:
        print(f"Error in generate_roadmap_route: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)