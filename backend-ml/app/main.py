from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import logging
import os

load_dotenv()

logger = logging.getLogger(__name__)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

# Global variables for loaded models and predictor
models = {}
predictor = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    await load_models()
    await initialize_predictor()
    yield
    # Shutdown
    await cleanup()


async def load_models():
    """Load all model files into memory at startup."""
    global models
    from .model_loader import ModelLoader
    logger.info("Loading models...")
    loader = ModelLoader()
    models = await loader.load_all()
    logger.info("Models loaded successfully")


async def initialize_predictor():
    """Initialize the PricePredictor with loaded models and feature engineer."""
    global predictor
    from .predictor import PricePredictor
    from .feature_engineer import load_feature_engineer

    logger.info("Initializing feature engineer...")
    models_dir = os.getenv("MODELS_DIR", os.path.join(os.path.dirname(__file__), "..", "models"))
    fe = load_feature_engineer(models_dir)

    logger.info("Initializing predictor...")
    predictor = PricePredictor(models, fe)
    logger.info("Predictor initialized successfully")


async def cleanup():
    """Release resources on shutdown."""
    global models, predictor
    logger.info("Cleaning up resources...")
    models.clear()
    predictor = None
    logger.info("Cleanup complete")


app = FastAPI(
    title="Price Model Server",
    description="ML model inference server for price predictions and anomaly detection",
    version="1.0.0",
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routes
from .routes import router
app.include_router(router)
