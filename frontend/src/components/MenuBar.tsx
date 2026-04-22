/** Top navigation bar with links to Cards, Reports, Dictionaries, Inventory, and User settings. */
import { AppBar, Toolbar, Typography, Menu, MenuItem, Button, IconButton } from "@mui/material";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import { Link, useNavigate } from "react-router";
import { useState } from "react";
import { useAuthStore } from "@/store/authStore";

const MenuBar = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [inventoryAnchorEl, setInventoryAnchorEl] = useState<null | HTMLElement>(null);
  const { user, clearAuth } = useAuthStore();
  const navigate = useNavigate();

  const handleCloseMenu = () => setAnchorEl(null);
  const handleCloseInventoryMenu = () => setInventoryAnchorEl(null);

  return (
    <AppBar position="sticky">
      <Toolbar className="justify-between">
        <div className="flex items-center gap-4">
          <IconButton edge="start" color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
            <Typography variant="subtitle2" style={{ marginRight: "4px", marginTop: "1px" }}>
              ČÍSELNÍKY
            </Typography>
            <ArrowDropDownIcon />
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleCloseMenu}>
            <MenuItem component={Link} to="/asset-group" onClick={handleCloseMenu}>
              Skupina
            </MenuItem>
            <MenuItem component={Link} to="/class" onClick={handleCloseMenu}>
              Třída
            </MenuItem>
            <MenuItem component={Link} to="/location" onClick={handleCloseMenu}>
              Umístění
            </MenuItem>
            <MenuItem component={Link} to="/purchase-method" onClick={handleCloseMenu}>
              Způsob pořízení
            </MenuItem>
            <MenuItem component={Link} to="/write-off-method" onClick={handleCloseMenu}>
              Způsob vyřazení
            </MenuItem>
          </Menu>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/card");
            }}>
            Majetkové karty
          </Button>
          <Button
            color="inherit"
            onClick={() => {
              navigate("/report");
            }}>
            Sestavy
          </Button>
          <IconButton color="inherit" onClick={(e) => setInventoryAnchorEl(e.currentTarget)}>
            <Typography variant="subtitle2" style={{ marginRight: "4px", marginTop: "1px" }}>
              INVENTURA
            </Typography>
            <ArrowDropDownIcon />
          </IconButton>
          <Menu anchorEl={inventoryAnchorEl} open={Boolean(inventoryAnchorEl)} onClose={handleCloseInventoryMenu}>
            <MenuItem component={Link} to="/inventory-audit/overview" onClick={handleCloseInventoryMenu}>
              Přehled
            </MenuItem>
            <MenuItem component={Link} to="/inventory-audit/differences" onClick={handleCloseInventoryMenu}>
              Rozdíly
            </MenuItem>
          </Menu>
        </div>
        <div className="flex items-center gap-4">
          <Button color="inherit" component={Link} to="/user">
            {user?.name}
          </Button>
          <Button
            color="inherit"
            onClick={() => {
              clearAuth();
              navigate("/");
            }}>
            Odhlásit se
          </Button>
        </div>
      </Toolbar>
    </AppBar>
  );
};

export default MenuBar;
